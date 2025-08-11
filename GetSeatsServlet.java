package com.example.airlinereservationsystem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

@WebServlet("/api/seats")
public class GetSeatsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String flightNumber = req.getParameter("flightNumber");
        String departure    = req.getParameter("departure"); // "2025-08-10 10:00"

        resp.setContentType("application/json");
        List<Map<String, String>> seats = new ArrayList<>();

        String sql = """
            SELECT s.seat_number, s.status, s.class
            FROM seats s
            JOIN flights f ON f.flight_id = s.flight_id
            WHERE f.flight_number = ? AND f.departure_time = ?
            ORDER BY s.seat_number
        """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, flightNumber);
            ps.setTimestamp(2, Timestamp.valueOf(departure));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> m = new HashMap<>();
                    m.put("seat",   rs.getString("seat_number"));
                    m.put("status", rs.getString("status"));
                    m.put("class",  rs.getString("class"));
                    seats.add(m);
                }
            }
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            return;
        }

        // naive JSON (enough for dev)
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < seats.size(); i++) {
            Map<String, String> s = seats.get(i);
            json.append(String.format(
                    "{\"seat\":\"%s\",\"status\":\"%s\",\"class\":\"%s\"}",
                    s.get("seat"), s.get("status"), s.get("class")));
            if (i < seats.size() - 1) json.append(",");
        }
        json.append("]");

        PrintWriter out = resp.getWriter();
        out.write(json.toString());
    }
}
