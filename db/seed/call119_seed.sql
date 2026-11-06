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
-- ----------------------------------------------------------------------------

-- 화재 신고 ------------------------------------------------------------------

INSERT INTO call_scenarios (call_title, situation_text)
SELECT '화재 신고', '집 부엌에서 불이 났어요. 연기가 나고 있어요.'
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
         , '["불", "화재", "연기", "타고", "탔"]' AS keywords
         , '무슨 일이 생겼는지 말해주세요.\n예) "불이 났어요"' AS weak_feedback
         , 30 AS score
    UNION ALL
    SELECT @fire_id, 2, 'LOCATION', '주소', '주소 말하기'
         , '정확한 주소를 말씀해주시겠어요?'
         , '역삼동 OO 아파트예요.'
         , '["동", "번지", "아파트", "빌라", "오피스텔", "건물", "길"]'
         , '동, 번지나 건물 이름을 말해주세요.\n예) "역삼동 123번지", "OO 아파트"'
         , 40
    UNION ALL
    SELECT @fire_id, 3, 'INJURY', '상태 설명', '상태 말하기'
         , '다친 사람이 있나요?'
         , '다친 사람은 없어요.'
         , '["다쳤", "다친", "다치", "부상", "아파", "아프", "피가", "괜찮", "없어"]'
         , '다친 사람이 있는지 말해주세요.\n예) "다친 사람은 없어요"'
         , 30
) AS items
 WHERE NOT EXISTS (SELECT 1
                     FROM call_simulator s
                    WHERE s.call_scenario_id = items.call_scenario_id
                      AND s.item_order = items.item_order);
