package com.nexus.sion.feature.squad.query.util;

import java.sql.*;

public class JdbcTest {
  public static void main(String[] args) {
    // 환경변수 읽기
    String dbHost = System.getenv("DB_HOST");
    String dbPort = System.getenv("DB_PORT");
    String dbName = System.getenv("DB_NAME");
    String dbUsername = System.getenv("DB_USERNAME");
    String dbPassword = System.getenv("DB_PASSWORD");

    // fallback (테스트 시 편의)
    if (dbHost == null) dbHost = "localhost";
    if (dbPort == null) dbPort = "3306";
    if (dbName == null) dbName = "SION";
    if (dbUsername == null) dbUsername = "root";
    if (dbPassword == null) dbPassword = "8897";

    String jdbcUrl = String.format("jdbc:mariadb://%s:%s/%s", dbHost, dbPort, dbName);

    try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)) {
      String sql =
          "SELECT project_and_job_id, job_name FROM project_and_job WHERE project_code = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, "PRJ001");

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        System.out.println(
            "project_and_job_id = "
                + rs.getString("project_and_job_id")
                + ", job_name = "
                + rs.getString("job_name"));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
