-- ----------------------------------------------------------------------------
-- 재난 시뮬레이션 채점 키워드 시드 데이터
--
-- 사용법
--   deundeun DB 에 접속해서 이 파일을 그대로 실행
--   같은 제목의 시나리오나 같은 순서의 키워드가 이미 있으면 건너뛰므로 여러 번 실행해도 됨
--
-- 작성 요령
--   - pose_title 은 서버 SimulationService 의 재난 종류 이름과 같아야 한다 ('지진 대피', '화재 대피')
--   - step_name 은 앱이 보내는 동작 이름이다 (CROUCH, COVER_HEAD, COVER_MOUTH)
--   - QUICK 은 빠른 반응 키워드로, 앱이 보낸 걸린 시간으로 채점한다 (20점)
--   - 나머지 80점은 동작 키워드끼리 똑같이 나눈다
--   - 자세 판정은 앱에서 하므로 angles_json 은 비워 둔다 ('{}')
-- ----------------------------------------------------------------------------

-- 지진 대피 ------------------------------------------------------------------

INSERT INTO pose_scenarios (pose_title)
SELECT '지진 대피'
 WHERE NOT EXISTS (SELECT 1 FROM pose_scenarios WHERE pose_title = '지진 대피');

SET @quake_id = (SELECT pose_scenario_id FROM pose_scenarios WHERE pose_title = '지진 대피');

INSERT INTO pose_steps (pose_scenario_id, step_order, step_name, label, guide_text, hold_sec, angles_json, weak_feedback)
SELECT * FROM (
    SELECT @quake_id AS pose_scenario_id
         , 1 AS step_order
         , 'CROUCH' AS step_name
         , '웅크리기' AS label
         , '무릎을 굽혀 몸을 낮춰요' AS guide_text
         , 3 AS hold_sec
         , '{}' AS angles_json
         , '무릎을 굽혀 몸을 더 낮춰 보세요.' AS weak_feedback
    UNION ALL
    SELECT @quake_id, 2, 'COVER_HEAD', '머리 보호', '두 팔로 머리를 감싸요', 3, '{}'
         , '두 팔로 머리를 꼭 감싸 보세요.'
    UNION ALL
    SELECT @quake_id, 3, 'QUICK', '빠른 반응', '흔들리면 바로 자세를 잡아요', 3, '{}'
         , '흔들리면 바로 탁자 밑으로 들어가 보세요.'
) AS steps
 WHERE NOT EXISTS (SELECT 1
                     FROM pose_steps p
                    WHERE p.pose_scenario_id = steps.pose_scenario_id
                      AND p.step_order = steps.step_order);

-- 화재 대피 ------------------------------------------------------------------

INSERT INTO pose_scenarios (pose_title)
SELECT '화재 대피'
 WHERE NOT EXISTS (SELECT 1 FROM pose_scenarios WHERE pose_title = '화재 대피');

SET @fire_id = (SELECT pose_scenario_id FROM pose_scenarios WHERE pose_title = '화재 대피');

INSERT INTO pose_steps (pose_scenario_id, step_order, step_name, label, guide_text, hold_sec, angles_json, weak_feedback)
SELECT * FROM (
    SELECT @fire_id AS pose_scenario_id
         , 1 AS step_order
         , 'COVER_MOUTH' AS step_name
         , '코·입 막기' AS label
         , '손으로 코와 입을 막아요' AS guide_text
         , 3 AS hold_sec
         , '{}' AS angles_json
         , '손으로 코와 입을 꼭 막아 보세요.' AS weak_feedback
    UNION ALL
    SELECT @fire_id, 2, 'QUICK', '빠른 반응', '연기가 나면 바로 코와 입을 막아요', 3, '{}'
         , '연기가 나면 바로 코와 입부터 막아 보세요.'
) AS steps
 WHERE NOT EXISTS (SELECT 1
                     FROM pose_steps p
                    WHERE p.pose_scenario_id = steps.pose_scenario_id
                      AND p.step_order = steps.step_order);
