import json
import re
from datetime import datetime
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry
import requests
from bs4 import BeautifulSoup

BASE_URL = "https://school.jbedu.kr/woosuk/M010501/list.do"

def crawl_and_standardize(year: int, month: int):
    params = {"y": year, "m": month}
    headers = {
        "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "Accept-Language": "ko-KR,ko;q=0.9",
    }

    session = requests.Session()
    retries = Retry(total=3, backoff_factor=1, status_forcelist=[500, 502, 503, 504])
    session.mount("https://", HTTPAdapter(max_retries=retries))

    try:
        res = session.get(BASE_URL, params=params, headers=headers, timeout=15)
        res.raise_for_status()
    except requests.exceptions.RequestException as e:
        print(f"네트워크 오류 발생: {e}")
        return None

    soup = BeautifulSoup(res.text, "html.parser")
    text = soup.get_text("\n", strip = True)

    # 날짜와 일정 패턴 추출
    pattern = re.compile(
        r"(\d{4}\.\d{2}\.\d{2}(?:\s*~\s*\d{4}\.\d{2}\.\d{2})?)\s*\n?-?\s*([^\n]+)"
    )
    matches = pattern.findall(text)

    exclude_keywords = {
        "학사일정", "교육활동", "우석고등학교", "메인메뉴", "본문내용", "퀵메뉴"
    }

    seen = set()
    schedules = []

    for date_str, event_str in matches:
        event = event_str.strip()
        if any(kw in event for kw in exclude_keywords):
            continue

        # 날짜 문자열 정규화 (공백 제거 및 점을 하이픈으로 변경)
        norm_date = re.sub(r"\s*~\s*", " ~ ", date_str.strip()).replace(".", "-")

        # 중복 방지 고유 키 (정규화된 날짜 + 타이틀)
        unique_key = (norm_date, event)
        if unique_key in seen:
            continue
        seen.add(unique_key)

        # 시작일/종료일 분리 및 기간 판별
        if " ~ " in norm_date:
            start_date, end_date = norm_date.split(" ~ ")
            is_period = True
        else:
            start_date = norm_date
            end_date = norm_date
            is_period = False

        # 고유 ID 생성 (날짜 + 키워드 해시 형태)
        event_id = f"{start_date}-{end_date}-{hash(event) & 0xffff}"

        schedules.append({
            "id": event_id,
            "title": event,
            "startDate": start_date,
            "endDate": end_date,
            "isPeriod": is_period
        })

    # 시작일 기준 정렬
    schedules.sort(key = lambda x: (x["startDate"], x["endDate"]))

    result = {
        "year": year,
        "month": month,
        "totalCount": len(schedules),
        "schedules": schedules
    }

    # JSON 파일 저장
    filename = f"schedule_{year}_{month:02d}.json"
    with open(filename, "w", encoding = "utf-8") as f:
        json.dump(result, f, ensure_ascii = False, indent = 4)

    print(f"[{year}년 {month}월] 총 {len(schedules)}개 항목 -> '{filename}' 저장")
    return result

if __name__ == "__main__":
    crawl_and_standardize(2026, 9)