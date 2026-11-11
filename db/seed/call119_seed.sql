-- ----------------------------------------------------------------------------
-- 119 신고 연습 시드 데이터
--
-- 사용법
--   deundeun DB 에 접속해서 이 파일을 그대로 실행
--   같은 제목의 시나리오나 같은 순서의 질문이 이미 있으면 건너뛰므로 여러 번 실행해도 됨
--
-- 키워드 작성 요령
--   - JSON 배열로 적는다. 서버는 공백을 뺀 답변에 키워드가 하나라도 있으면 통과로 본다
--   - 활용형까지 잡히도록 어간만 적는다 (다쳤어요, 다쳤습니다 → "다쳤")
--   - "요", "이" 처럼 어디에나 나오는 글자는 넣지 않는다
--   - 단서를 모두 말해야 하는 질문은 {"parts": [{"words": [...], "ask": "되묻는 말"}, ...]} 로 적는다
--     묶음마다 키워드가 하나라도 나와야 하고, 일부만 말하면 빠진 묶음의 ask 로 되묻는다
--     ask 에는 답을 알려주지 말고 어디를 보라고만 적는다 (예: "주변 건물은 어떤가요?")
--
-- 상황(situation_text) 작성 요령
--   - 화면에 단서 칩으로 보여준다. 그대로 읽는 대신 자기 말로 설명하게 문장이 아닌 짧은 낱말로 적는다
--   - 단서는 " · " 로 나눈다 (예: '집 · 큰 비 · 물이 차오름')
-- ----------------------------------------------------------------------------

-- 화재 신고 ------------------------------------------------------------------

INSERT INTO call_scenarios (call_title, situation_text)
SELECT '화재 신고', '집 부엌 · 불 · 연기'
 WHERE NOT EXISTS (SELECT 1 FROM call_scenarios WHERE call_title = '화재 신고');

SET @fire_id = (SELECT call_scenario_id FROM call_scenarios WHERE call_title = '화재 신고');

INSERT INTO call_simulator (
    call_scenario_id
  , item_order
  , item_type
  , label
  , stat_label
  , question
  , answer
  , keywords
  , weak_feedback
  , score
)
SELECT * FROM (
    SELECT @fire_id AS call_scenario_id
         , 1 AS item_order
         , 'SITUATION' AS item_type
         , '상황' AS label
         , '상황 말하기' AS stat_label
         , '네, 119입니다. 무슨 일이세요?' AS question
         , '집에 불이 났어요.' AS answer
         , '{"parts": [{"words": ["불", "화재", "타고", "탔", "타요", "불길"], "ask": "어떤 일이 일어났는지 조금 더 말해 주세요."}, {"words": ["연기", "냄새", "매워", "매캐"], "ask": "불 말고 또 보이는 게 있나요?"}]}' AS keywords
         , '무슨 일이 생겼는지 말해주세요.\n예) "부엌에 불이 나서 연기가 나요"' AS weak_feedback
         , 30 AS score
    UNION ALL
    SELECT @fire_id, 2, 'LOCATION', '주소', '주소 말하기'
         , '정확한 주소를 말씀해주시겠어요?'
         , '역삼동 OO 아파트예요.'
         , '{"parts": [{"words": ["동", "읍", "번지", "번길", "길", "대로", "층", "아파트", "빌라", "오피스텔", "주택", "상가", "빌딩", "학교", "초등", "중학", "고등", "대학", "유치원", "어린이집", "병원", "의원", "약국", "역", "정류장", "터미널", "마트", "시장", "편의점", "공원", "교회", "성당", "도서관", "센터", "회사", "식당", "가게", "카페", "앞", "옆", "근처", "건너편", "사거리"]}, {"words": ["동", "읍", "번지", "번길", "길", "대로", "층", "아파트", "빌라", "오피스텔", "주택", "상가", "빌딩", "학교", "초등", "중학", "고등", "대학", "유치원", "어린이집", "병원", "의원", "약국", "역", "정류장", "터미널", "마트", "시장", "편의점", "공원", "교회", "성당", "도서관", "센터", "회사", "식당", "가게", "카페", "앞", "옆", "근처", "건너편", "사거리"], "named": true, "ask": "어느 곳인지 이름이나 동네 이름도 같이 알려 주세요."}]}'
         , '어느 동네인지, 근처에 보이는 큰 건물이나 가게 이름을 말해주세요.\n예) "역삼동 OO 아파트", "OO초등학교 앞"'
         , 40
    UNION ALL
    SELECT @fire_id, 3, 'INJURY', '상태 설명', '상태 말하기'
         , '다친 사람이 있나요?'
         , '다친 사람은 없어요.'
         , '["다쳤", "다친", "다치", "부상", "아파", "아프", "피가", "괜찮", "없", "있어", "있습", "있다", "네", "예", "응", "아니", "명", "아무도"]'
         , '다친 사람이 있는지 말해주세요.\n예) "네, 한 명 다쳤어요", "다친 사람은 없어요"'
         , 30
) AS items
 WHERE NOT EXISTS (SELECT 1
                     FROM call_simulator s
                    WHERE s.call_scenario_id = items.call_scenario_id
                      AND s.item_order = items.item_order);

-- 지진 신고 ------------------------------------------------------------------

INSERT INTO call_scenarios (call_title, situation_text)
SELECT '지진 신고', '땅 흔들림 · 건물 무너짐'
 WHERE NOT EXISTS (SELECT 1 FROM call_scenarios WHERE call_title = '지진 신고');

SET @quake_id = (SELECT call_scenario_id FROM call_scenarios WHERE call_title = '지진 신고');

INSERT INTO call_simulator (
    call_scenario_id
  , item_order
  , item_type
  , label
  , stat_label
  , question
  , answer
  , keywords
  , weak_feedback
  , score
)
SELECT * FROM (
    SELECT @quake_id AS call_scenario_id
         , 1 AS item_order
         , 'SITUATION' AS item_type
         , '상황' AS label
         , '상황 말하기' AS stat_label
         , '네, 119입니다. 무슨 일이세요?' AS question
         , '지진이 나서 건물이 무너졌어요.' AS answer
         , '{"parts": [{"words": ["지진", "흔들", "진동"], "ask": "땅은 어떤가요?"}, {"words": ["무너", "붕괴", "깔렸", "깔려", "갈라", "금이", "부서"], "ask": "주변 건물은 어떤가요?"}]}' AS keywords
         , '무슨 일이 생겼는지 말해주세요.\n예) "땅이 흔들리고 건물이 무너졌어요"' AS weak_feedback
         , 30 AS score
    UNION ALL
    SELECT @quake_id, 2, 'LOCATION', '주소', '주소 말하기'
         , '정확한 주소를 말씀해주시겠어요?'
         , '역삼동 OO 아파트예요.'
         , '{"parts": [{"words": ["동", "읍", "번지", "번길", "길", "대로", "층", "아파트", "빌라", "오피스텔", "주택", "상가", "빌딩", "학교", "초등", "중학", "고등", "대학", "유치원", "어린이집", "병원", "의원", "약국", "역", "정류장", "터미널", "마트", "시장", "편의점", "공원", "교회", "성당", "도서관", "센터", "회사", "식당", "가게", "카페", "앞", "옆", "근처", "건너편", "사거리"]}, {"words": ["동", "읍", "번지", "번길", "길", "대로", "층", "아파트", "빌라", "오피스텔", "주택", "상가", "빌딩", "학교", "초등", "중학", "고등", "대학", "유치원", "어린이집", "병원", "의원", "약국", "역", "정류장", "터미널", "마트", "시장", "편의점", "공원", "교회", "성당", "도서관", "센터", "회사", "식당", "가게", "카페", "앞", "옆", "근처", "건너편", "사거리"], "named": true, "ask": "어느 곳인지 이름이나 동네 이름도 같이 알려 주세요."}]}'
         , '어느 동네인지, 근처에 보이는 큰 건물이나 가게 이름을 말해주세요.\n예) "역삼동 OO 아파트", "OO초등학교 앞"'
         , 40
    UNION ALL
    SELECT @quake_id, 3, 'INJURY', '상태 설명', '상태 말하기'
         , '갇히거나 다친 사람이 있나요?'
         , '한 명이 갇혀 있어요.'
         , '["갇혔", "갇혀", "갇힌", "깔렸", "깔려", "못 나", "못나", "다쳤", "다친", "다치", "부상", "아파", "아프", "피가", "괜찮", "없", "있어", "있습", "있다", "네", "예", "응", "아니", "명", "아무도"]'
         , '갇히거나 다친 사람이 있는지 말해주세요.\n예) "한 명이 갇혀 있어요", "다친 사람은 없어요"'
         , 30
) AS items
 WHERE NOT EXISTS (SELECT 1
                     FROM call_simulator s
                    WHERE s.call_scenario_id = items.call_scenario_id
                      AND s.item_order = items.item_order);

-- 홍수 신고 ------------------------------------------------------------------

INSERT INTO call_scenarios (call_title, situation_text)
SELECT '홍수 신고', '집 · 큰 비 · 물이 차오름'
 WHERE NOT EXISTS (SELECT 1 FROM call_scenarios WHERE call_title = '홍수 신고');

SET @flood_id = (SELECT call_scenario_id FROM call_scenarios WHERE call_title = '홍수 신고');

INSERT INTO call_simulator (
    call_scenario_id
  , item_order
  , item_type
  , label
  , stat_label
  , question
  , answer
  , keywords
  , weak_feedback
  , score
)
SELECT * FROM (
    SELECT @flood_id AS call_scenario_id
         , 1 AS item_order
         , 'SITUATION' AS item_type
         , '상황' AS label
         , '상황 말하기' AS stat_label
         , '네, 119입니다. 무슨 일이세요?' AS question
         , '집에 물이 차고 있어요.' AS answer
         , '{"parts": [{"words": ["비", "폭우", "장마", "홍수", "태풍"], "ask": "밖에 날씨는 어떤가요?"}, {"words": ["물", "잠겼", "잠겨", "잠기", "침수", "차오", "넘쳐", "넘치"], "ask": "집 안은 어떤가요?"}]}' AS keywords
         , '무슨 일이 생겼는지 말해주세요.\n예) "비가 많이 와서 집에 물이 차고 있어요"' AS weak_feedback
         , 30 AS score
    UNION ALL
    SELECT @flood_id, 2, 'LOCATION', '주소', '주소 말하기'
         , '정확한 주소를 말씀해주시겠어요?'
         , '역삼동 OO 빌라예요.'
         , '{"parts": [{"words": ["동", "읍", "번지", "번길", "길", "대로", "층", "아파트", "빌라", "오피스텔", "주택", "상가", "빌딩", "학교", "초등", "중학", "고등", "대학", "유치원", "어린이집", "병원", "의원", "약국", "역", "정류장", "터미널", "마트", "시장", "편의점", "공원", "교회", "성당", "도서관", "센터", "회사", "식당", "가게", "카페", "앞", "옆", "근처", "건너편", "사거리"]}, {"words": ["동", "읍", "번지", "번길", "길", "대로", "층", "아파트", "빌라", "오피스텔", "주택", "상가", "빌딩", "학교", "초등", "중학", "고등", "대학", "유치원", "어린이집", "병원", "의원", "약국", "역", "정류장", "터미널", "마트", "시장", "편의점", "공원", "교회", "성당", "도서관", "센터", "회사", "식당", "가게", "카페", "앞", "옆", "근처", "건너편", "사거리"], "named": true, "ask": "어느 곳인지 이름이나 동네 이름도 같이 알려 주세요."}]}'
         , '어느 동네인지, 근처에 보이는 큰 건물이나 가게 이름을 말해주세요.\n예) "역삼동 OO 빌라", "OO초등학교 앞"'
         , 40
    UNION ALL
    SELECT @flood_id, 3, 'INJURY', '상태 설명', '상태 말하기'
         , '갇히거나 다친 사람이 있나요?'
         , '물 때문에 밖으로 못 나가고 있어요.'
         , '["갇혔", "갇혀", "갇힌", "못 나", "못나", "고립", "다쳤", "다친", "다치", "부상", "아파", "아프", "괜찮", "없", "있어", "있습", "있다", "네", "예", "응", "아니", "명", "아무도"]'
         , '갇히거나 다친 사람이 있는지 말해주세요.\n예) "밖으로 못 나가고 있어요", "다친 사람은 없어요"'
         , 30
) AS items
 WHERE NOT EXISTS (SELECT 1
                     FROM call_simulator s
                    WHERE s.call_scenario_id = items.call_scenario_id
                      AND s.item_order = items.item_order);
