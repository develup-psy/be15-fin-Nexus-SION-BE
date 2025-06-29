package com.nexus.sion.feature.squad.query.util;

import java.sql.*;

public class JdbcTest {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mariadb://localhost:3306/SION";
        String username = "root"; // ← application.yml에 설정된 DB 계정
        String password = "8897";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            String sql = "SELECT project_and_job_id, job_name FROM project_and_job WHERE project_code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "PRJ001");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("project_and_job_id = " + rs.getString("project_and_job_id") +
                        ", job_name = " + rs.getString("job_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

