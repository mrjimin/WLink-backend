import hashlib
import os
import re
from datetime import date

import psycopg
import requests
from bs4 import BeautifulSoup
from dotenv import load_dotenv
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry


BASE_URL = "https://school.jbedu.kr/woosuk/M010501/list.do"

CRAWL_MONTHS = 3
REQUEST_TIMEOUT = 15

REQUEST_HEADERS = {
    "User-Agent": "Mozilla/5.0",
    "Accept-Language": "ko-KR,ko;q=0.9",
}

DATE_PATTERN = re.compile(
    r"(\d{4}\.\d{2}\.\d{2}"
    r"(?:\s*~\s*\d{4}\.\d{2}\.\d{2})?)"
    r"\s*\n?-?\s*([^\n]+)"
)

EXCLUDED_KEYWORDS = {
    "학사일정",
    "교육활동",
    "우석고등학교",
    "메인메뉴",
    "본문내용",
    "퀵메뉴",
}


load_dotenv()


def get_db_config():
    return {
        "host": os.environ["DB_HOST"],
        "port": os.environ.get("DB_PORT", "5432"),
        "dbname": os.environ["DB_NAME"],
        "user": os.environ["DB_USER"],
        "password": os.environ["DB_PASSWORD"],
    }


def create_session():
    session = requests.Session()

    retry = Retry(
        total=3,
        backoff_factor=1,
        status_forcelist=(500, 502, 503, 504),
        allowed_methods=("GET",),
    )

    session.mount(
        "https://",
        HTTPAdapter(max_retries=retry),
    )

    return session


def crawl_month(year, month, session):
    response = session.get(
        BASE_URL,
        params={
            "y": year,
            "m": month,
        },
        headers=REQUEST_HEADERS,
        timeout=REQUEST_TIMEOUT,
    )
    response.raise_for_status()

    text = BeautifulSoup(
        response.text,
        "html.parser",
    ).get_text("\n", strip=True)

    unique_schedules = {}

    for date_text, title in DATE_PATTERN.findall(text):
        title = title.strip()

        if not title:
            continue

        if any(
                keyword in title
                for keyword in EXCLUDED_KEYWORDS
        ):
            continue

        start_text, _, end_text = (
            date_text
            .replace(".", "-")
            .partition("~")
        )

        try:
            start_date = date.fromisoformat(
                start_text.strip()
            )
            end_date = date.fromisoformat(
                end_text.strip() or start_text.strip()
            )
        except ValueError:
            continue

        if end_date < start_date:
            continue

        key = (
            start_date,
            end_date,
            title,
        )

        unique_schedules[key] = {
            "title": title,
            "start_date": start_date,
            "end_date": end_date,
            "is_period": start_date != end_date,
        }

    return list(unique_schedules.values())


def create_event_id(schedule):
    raw = "|".join(
        (
            str(schedule["start_date"]),
            str(schedule["end_date"]),
            schedule["title"],
        )
    )

    return hashlib.sha256(
        raw.encode("utf-8")
    ).hexdigest()


def save_schedules(schedules, conn):
    rows = [
        (
            create_event_id(schedule),
            schedule["title"],
            schedule["start_date"],
            schedule["end_date"],
            schedule["is_period"],
        )
        for schedule in schedules
    ]

    with conn.cursor() as cursor:
        cursor.execute(
            "TRUNCATE TABLE schedules"
        )

        if rows:
            cursor.executemany(
                """
                INSERT INTO schedules (
                    id,
                    title,
                    start_date,
                    end_date,
                    is_period
                )
                VALUES (%s, %s, %s, %s, %s)
                """,
                rows,
            )


def add_months(year, month, months):
    month_index = year * 12 + month - 1 + months

    return (
        month_index // 12,
        month_index % 12 + 1,
    )


def collect_schedules(months, session):
    today = date.today()
    unique_schedules = {}

    for offset in range(months):
        year, month = add_months(
            today.year,
            today.month,
            offset,
        )

        print(f"[CRAWL] {year}-{month:02d}")

        month_schedules = crawl_month(
            year,
            month,
            session,
        )

        print(
            f"[FOUND] {len(month_schedules)} schedules"
        )

        for schedule in month_schedules:
            key = (
                schedule["start_date"],
                schedule["end_date"],
                schedule["title"],
            )

            unique_schedules[key] = schedule

    return sorted(
        unique_schedules.values(),
        key=lambda schedule: (
            schedule["start_date"],
            schedule["end_date"],
            schedule["title"],
        ),
    )


def main():
    with create_session() as session:
        schedules = collect_schedules(
            CRAWL_MONTHS,
            session,
        )

    print(f"[TOTAL] {len(schedules)} schedules")

    with psycopg.connect(**get_db_config()) as conn:
        save_schedules(schedules, conn)

    print("[DONE]")


if __name__ == "__main__":
    main()
