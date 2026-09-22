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

load_dotenv()

DB_CONFIG = {
    "host": os.environ["DB_HOST"],
    "port": os.environ.get("DB_PORT"),
    "dbname": os.environ["DB_NAME"],
    "user": os.environ["DB_USER"],
    "password": os.environ["DB_PASSWORD"],
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


def crawl(year, month, session):
    response = session.get(
        BASE_URL,
        params={"y": year, "m": month},
        headers={
            "User-Agent": "Mozilla/5.0",
            "Accept-Language": "ko-KR,ko;q=0.9",
        },
        timeout=15,
    )
    response.raise_for_status()

    text = BeautifulSoup(
        response.text,
        "html.parser",
    ).get_text("\n", strip=True)

    schedules = {}

    for date_text, title in DATE_PATTERN.findall(text):
        title = title.strip()

        if not title or any(
                keyword in title
                for keyword in EXCLUDED_KEYWORDS
        ):
            continue

        start, _, end = date_text.replace(".", "-").partition("~")

        start_date = date.fromisoformat(start.strip())
        end_date = date.fromisoformat(
            end.strip() or start.strip()
        )

        if end_date < start_date:
            continue

        key = (
            start_date,
            end_date,
            title,
        )

        schedules[key] = {
            "title": title,
            "start_date": start_date,
            "end_date": end_date,
            "is_period": start_date != end_date,
        }

    return list(schedules.values())


def event_id(schedule):
    raw = (
        f'{schedule["start_date"]}|'
        f'{schedule["end_date"]}|'
        f'{schedule["title"]}'
    )

    return hashlib.sha256(
        raw.encode()
    ).hexdigest()


def save(schedules, conn):
    with conn.cursor() as cur:
        cur.execute("TRUNCATE TABLE schedules")

        if schedules:
            cur.executemany(
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
                [
                    (
                        event_id(schedule),
                        schedule["title"],
                        schedule["start_date"],
                        schedule["end_date"],
                        schedule["is_period"],
                    )
                    for schedule in schedules
                ],
            )

    conn.commit()


def add_months(year, month, months):
    index = year * 12 + month - 1 + months
    return index // 12, index % 12 + 1


def main(months=3):
    today = date.today()
    schedules = {}

    with (
        create_session() as session,
        psycopg.connect(**DB_CONFIG) as conn,
    ):
        for offset in range(months):
            year, month = add_months(
                today.year,
                today.month,
                offset,
            )

            print(f"[CRAWL] {year}-{month:02d}")

            month_schedules = crawl(
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
                schedules[key] = schedule

        schedules = sorted(
            schedules.values(),
            key=lambda schedule: (
                schedule["start_date"],
                schedule["end_date"],
                schedule["title"],
            ),
        )

        print(f"[TOTAL] {len(schedules)} schedules")

        save(schedules, conn)

    print("[DONE]")


if __name__ == "__main__":
    main()
