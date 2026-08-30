-- KCD(한국표준질병분류) 샘플 데이터
-- 실제 운영 시에는 건강보험심사평가원 공식 데이터를 적재합니다.

INSERT INTO kcd_code (code, name_korean, name_english, category, description, active, created_date, modified_date) VALUES
-- 감염병 (A00-B99)
('A09', '감염성으로 추정되는 설사 및 위장염', 'Diarrhea and gastroenteritis of presumed infectious origin', 'A00-B99', '장관 감염질환', true, NOW(), NOW()),
('A16.9', '호흡기 결핵, 상세불명', 'Respiratory tuberculosis, unspecified', 'A00-B99', '결핵', true, NOW(), NOW()),

-- 신생물 (C00-D48)
('C34.9', '기관지 및 폐의 악성 신생물, 상세불명', 'Malignant neoplasm of bronchus and lung, unspecified', 'C00-D48', '폐암', true, NOW(), NOW()),
('C50.9', '유방의 악성 신생물, 상세불명', 'Malignant neoplasm of breast, unspecified', 'C00-D48', '유방암', true, NOW(), NOW()),

-- 내분비/영양/대사 (E00-E90)
('E10', '인슐린-의존성 당뇨병', 'Type 1 diabetes mellitus', 'E00-E90', '제1형 당뇨', true, NOW(), NOW()),
('E11', '인슐린-비의존성 당뇨병', 'Type 2 diabetes mellitus', 'E00-E90', '제2형 당뇨', true, NOW(), NOW()),
('E78.0', '순수 고콜레스테롤혈증', 'Pure hypercholesterolemia', 'E00-E90', '고지혈증', true, NOW(), NOW()),

-- 순환계통 (I00-I99)
('I10', '본태성(일차성) 고혈압', 'Essential (primary) hypertension', 'I00-I99', '고혈압', true, NOW(), NOW()),
('I21.9', '급성 심근경색증, 상세불명', 'Acute myocardial infarction, unspecified', 'I00-I99', '심근경색', true, NOW(), NOW()),
('I63.9', '뇌경색증, 상세불명', 'Cerebral infarction, unspecified', 'I00-I99', '뇌졸중', true, NOW(), NOW()),

-- 호흡계통 (J00-J99)
('J06.9', '급성 상기도감염, 상세불명', 'Acute upper respiratory infection, unspecified', 'J00-J99', '감기', true, NOW(), NOW()),
('J18.9', '폐렴, 상세불명', 'Pneumonia, unspecified', 'J00-J99', '폐렴', true, NOW(), NOW()),
('J45', '천식', 'Asthma', 'J00-J99', '천식', true, NOW(), NOW()),

-- 소화계통 (K00-K93)
('K21', '위-식도 역류질환', 'Gastro-esophageal reflux disease', 'K00-K93', '역류성 식도염', true, NOW(), NOW()),
('K29.5', '만성 위염, 상세불명', 'Chronic gastritis, unspecified', 'K00-K93', '만성 위염', true, NOW(), NOW()),

-- 근골격계통 (M00-M99)
('M54.5', '요통', 'Low back pain', 'M00-M99', '허리 통증', true, NOW(), NOW()),
('M79.3', '지정되지 않은 부위의 지방층염', 'Panniculitis, unspecified', 'M00-M99', '근막통증', true, NOW(), NOW()),

-- 정신 및 행동장애 (F00-F99)
('F32', '우울 에피소드', 'Depressive episode', 'F00-F99', '우울증', true, NOW(), NOW()),
('F41.1', '범불안 장애', 'Generalized anxiety disorder', 'F00-F99', '불안장애', true, NOW(), NOW());
