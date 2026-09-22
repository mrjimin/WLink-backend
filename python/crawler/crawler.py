import os
import psycopg
import re
import requests
from bs4 import BeautifulSoup
from datetime import date
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


def get_required_env(name):
    value = os.getenv(name)

    if not value:
        raise RuntimeError(
            f"Environment variable '{name}' is not set."
        )

    return value


def get_db_config():
    return {
        "host": get_required_env("DB_HOST"),
        "port": os.getenv("DB_PORT", "5432"),
        "dbname": get_required_env("DB_NAME"),
        "user": get_required_env("DB_USER"),
        "password": get_required_env("DB_PASSWORD"),
    }


def create_session():
    session = requests.Session()

    retry = Retry(
        total=3,
        connect=3,
        read=3,
        backoff_factor=1,
        status_forcelist=(500, 502, 503, 504),
        allowed_methods=("GET",),
    )

    adapter = HTTPAdapter(
        max_retries=retry
    )

    session.mount(
        "https://",
        adapter,
    )

    return session


def parse_date_range(date_text):
    start_text, _, end_text = (
        date_text
        .replace(".", "-")
        .partition("~")
    )

    start_date = date.fromisoformat(
        start_text.strip()
    )

    end_date = date.fromisoformat(
        end_text.strip() or start_text.strip()
    )

    if end_date < start_date:
        return None

    return start_date, end_date


def is_valid_title(title):
    if not title:
        return False

    return not any(
        keyword in title
        for keyword in EXCLUDED_KEYWORDS
    )


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
    ).get_text(
        "\n",
        strip=True,
    )

    schedules_by_key = {}

    for date_text, raw_title in DATE_PATTERN.findall(text):
        title = raw_title.strip()

        if not is_valid_title(title):
            continue

        try:
            date_range = parse_date_range(
                date_text
            )
        except ValueError:
            continue

        if date_range is None:
            continue

        start_date, end_date = date_range

        key = (
            start_date,
            end_date,
            title,
        )

        schedules_by_key[key] = {
            "title": title,
            "start_date": start_date,
            "end_date": end_date,
            "is_period": start_date != end_date,
        }

    return list(schedules_by_key.values())


def add_months(year, month, offset):
    month_index = (
            year * 12
            + month
            - 1
            + offset
    )

    return (
        month_index // 12,
        month_index % 12 + 1,
    )


def schedule_key(schedule):
    return (
        schedule["start_date"],
        schedule["end_date"],
        schedule["title"],
    )


def collect_schedules(months, session):
    today = date.today()
    schedules_by_key = {}

    for offset in range(months):
        year, month = add_months(
            today.year,
            today.month,
            offset,
        )

        print(
            f"[CRAWL] {year}-{month:02d}"
        )

        month_schedules = crawl_month(
            year,
            month,
            session,
        )

        print(
            f"[FOUND] {len(month_schedules)} schedules"
        )

        for schedule in month_schedules:
            schedules_by_key[
                schedule_key(schedule)
            ] = schedule

    return sorted(
        schedules_by_key.values(),
        key=lambda schedule: (
            schedule["start_date"],
            schedule["end_date"],
            schedule["title"],
        ),
    )


def save_schedules(schedules, conn):
    rows = [
        (
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
                    title,
                    start_date,
                    end_date,
                    is_period
                )
                VALUES (%s, %s, %s, %s)
                """,
                rows,
            )


def main():
    print("[START] School Schedule Crawler")

    with create_session() as session:
        schedules = collect_schedules(
            CRAWL_MONTHS,
            session,
        )

    print(
        f"[TOTAL] {len(schedules)} schedules"
    )

    with psycopg.connect(
            **get_db_config()
    ) as conn:
        save_schedules(
            schedules,
            conn,
        )

    print("[DONE]")


if __name__ == "__main__":
    main()
