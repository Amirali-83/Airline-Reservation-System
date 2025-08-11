package com.example.airlinereservationsystem;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

@WebServlet("/init-db")
public class TestDbServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1) create table (runs safely even if it exists)
        String create = """
            CREATE TABLE IF NOT EXISTS flights (
              id IDENTITY PRIMARY KEY,
              flight_no VARCHAR(10) NOT NULL,
              origin VARCHAR(3) NOT NULL,
              destination VARCHAR(3) NOT NULL,
              depart_time TIMESTAMP NOT NULL,
              arrive_time TIMESTAMP NOT NULL,
              duration_minutes INT NOT NULL,
              cabin VARCHAR(20) NOT NULL,
              price DECIMAL(10,2) NOT NULL,
              seats_left INT NOT NULL
            );
        """;

        // 2) put some rows (delete old rows first so we don’t duplicate)
        String wipe = "DELETE FROM flights";
        String insert = "INSERT INTO flights (flight_no, origin, destination, depart_time, arrive_time, duration_minutes, cabin, price, seats_left) VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection con = Db.getConnection()) {
            try (Statement st = con.createStatement()) { st.execute(create); st.execute(wipe); }

            try (PreparedStatement ps = con.prepareStatement(insert)) {
                // date: 2025-08-10  (so search that day)
                add(ps, "XY101","LAX","JFK","2025-08-10T08:00","2025-08-10T16:00",480,"ECONOMY", 350.00, 50);
                add(ps, "XY102","LAX","JFK","2025-08-10T10:00","2025-08-10T18:00",480,"BUSINESS",1200.00, 5);
                add(ps, "XY103","LAX","ORD","2025-08-10T09:30","2025-08-10T15:00",330,"ECONOMY", 280.00,80);
                add(ps, "XY200","LHR","JFK","2025-08-10T09:30","2025-08-10T12:25",475,"ECONOMY",520.00,18);
                add(ps, "XY201","LHR","JFK","2025-08-10T09:30","2025-08-10T12:25",475,"BUSINESS",1490.00, 6);
            }
            resp.setContentType("text/plain");
            resp.getWriter().println("DB ready. Seed flights inserted ✔");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().println("Init failed: " + e.getMessage());
        }
    }

    private void add(PreparedStatement ps, String no, String from, String to,
                     String dep, String arr, int mins, String cabin,
                     double price, int left) throws Exception {
        ps.setString(1, no);
        ps.setString(2, from);
        ps.setString(3, to);
        ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.parse(dep)));
        ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.parse(arr)));
        ps.setInt(6, mins);
        ps.setString(7, cabin);
        ps.setBigDecimal(8, new java.math.BigDecimal(price));
        ps.setInt(9, left);
        ps.addBatch();
        ps.executeBatch();
    }
}
