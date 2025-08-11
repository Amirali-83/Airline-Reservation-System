package com.example.airlinereservationsystem;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "ReserveSeatServlet", urlPatterns = "/reserve-seat")
public class ReserveSeatServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        String seatNumber = req.getParameter("seatNumber");
        String flightNo   = req.getParameter("flight");

        try (PrintWriter out = resp.getWriter()) {
            if (seatNumber == null || seatNumber.isBlank() ||
                    flightNo == null || flightNo.isBlank()) {
                resp.setStatus(400);
                out.print("{\"ok\":false,\"msg\":\"seatNumber and flight are required\"}");
                return;
            }

            String sql = """
                UPDATE seats s
                SET status = 'Occupied'
                WHERE s.seat_number = ?
                  AND s.flight_id = (
                      SELECT flight_id FROM flights WHERE flight_number = ?
                  )
                  AND s.status <> 'Occupied'
            """;

            int updated;
            try (Connection c = Db.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, seatNumber);
                ps.setString(2, flightNo);
                updated = ps.executeUpdate();
            }

            if (updated == 1) {
                resp.setStatus(200);
                out.print("{\"ok\":true}");
            } else {
                resp.setStatus(409);
                out.print("{\"ok\":false,\"msg\":\"Seat not found or already occupied\"}");
            }
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().print("{\"ok\":false,\"msg\":\"" + e.getMessage().replace("\"","\\\"") + "\"}");
        }
    }
}
