INSERT INTO grade (grade_code, min_score, max_score, productivity, monthly_unit_price)
VALUES ('A', 80, 100, 1.2, 5000000);

INSERT INTO member (
    employee_identification_number, employee_name, password, phone_number, joined_at,
    email, career_years, salary, status, grade_code,
    created_at, updated_at, role
) VALUES
      ('DEV001', '홍길동', 'password1', '01012345678', now(),
       'hong@example.com', 3, 6000000, 'AVAILABLE', 'A', now(), now(), 'INSIDER'),
      ('DEV002', '김철수', 'password2', '01022223333', now(),
       'kim@example.com', 5, 7000000, 'IN_PROJECT', 'A', now(), now(), 'INSIDER');