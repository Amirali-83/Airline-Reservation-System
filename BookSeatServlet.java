package com.example.airlinereservationsystem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/api/book-seat")
public class BookSeatServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String flightNumber = req.getParameter("flightNumber");
        String departure    = req.getParameter("departure"); // "2025-08-10 10:00"
        String seat         = req.getParameter("seat");      // e.g. "1A"
        String paxName      = req.getParameter("name");      // simple demo

        String findSeatSql = """
            SELECT s.seat_id, s.status
            FROM seats s
            JOIN flights f ON f.flight_id=s.flight_id
            WHERE f.flight_number=? AND f.departure_time=? AND s.seat_number=?
            FOR UPDATE
        """;

        String upSeatSql = "UPDATE seats SET status='Occupied' WHERE seat_id=? AND status='Available'";
        String insPaxSql = "INSERT INTO passengers(name, email) VALUES(?, '') RETURNING passenger_id";
        String insBkSql  = "INSERT INTO bookings(passenger_id, flight_id, seat_id) " +
                "VALUES(?, (SELECT flight_id FROM flights WHERE flight_number=? AND departure_time=?), ?)";

        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);

            int seatId = -1;
            try (PreparedStatement ps = con.prepareStatement(findSeatSql)) {
                ps.setString(1, flightNumber);
                ps.setTimestamp(2, Timestamp.valueOf(departure));
                ps.setString(3, seat);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if (!"Available".equalsIgnoreCase(status)) {
                            resp.setStatus(409);
                            resp.getWriter().write("{\"ok\":false,\"msg\":\"Seat not available\"}");
                            con.rollback();
                            return;
                        }
                        seatId = rs.getInt("seat_id");
                    } else {
                        resp.setStatus(404);
                        resp.getWriter().write("{\"ok\":false,\"msg\":\"Seat not found\"}");
                        con.rollback();
                        return;
                    }
                }
            }

            int paxId;
            try (PreparedStatement ps = con.prepareStatement(insPaxSql)) {
                ps.setString(1, paxName);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    paxId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(upSeatSql)) {
                ps.setInt(1, seatId);
                int updated = ps.executeUpdate();
                if (updated != 1) {
                    resp.setStatus(409);
                    resp.getWriter().write("{\"ok\":false,\"msg\":\"Seat already taken\"}");
                    con.rollback();
                    return;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(insBkSql)) {
                ps.setInt(1, paxId);
                ps.setString(2, flightNumber);
                ps.setTimestamp(3, Timestamp.valueOf(departure));
                ps.setInt(4, seatId);
                ps.executeUpdate();
            }

            con.commit();
            resp.setContentType("application/json");
            resp.getWriter().write("{\"ok\":true}");
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"ok\":false,\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
