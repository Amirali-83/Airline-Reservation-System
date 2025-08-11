package com.example.airlinereservationsystem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/select-seat")
public class SeatSelectionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String flightNo = request.getParameter("flightNo");
        if (flightNo == null || flightNo.isBlank()) {
            flightNo = "XY999"; // default demo flight
        }

        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.seat_number, s.status, s.class
            FROM seats s
            JOIN flights f ON f.flight_id = s.flight_id
            WHERE f.flight_number = ?
            ORDER BY s.seat_number
        """;

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, flightNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Seat s = new Seat();
                    s.setSeatNumber(rs.getString("seat_number"));
                    s.setStatus(rs.getString("status"));
                    s.setSeatClass(rs.getString("class"));
                    seats.add(s);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Failed to load seats", e);
        }

        // Fallback demo seats if DB returned nothing (so the page always renders)
        if (seats.isEmpty()) {
            Seat a = new Seat(); a.setSeatNumber("1A"); a.setSeatClass("Business"); a.setStatus("Available");
            Seat b = new Seat(); b.setSeatNumber("1B"); b.setSeatClass("Business"); b.setStatus("Occupied");
            Seat c = new Seat(); c.setSeatNumber("7C"); c.setSeatClass("Economy");  c.setStatus("Available");
            Seat d = new Seat(); d.setSeatNumber("12D"); d.setSeatClass("Economy"); d.setStatus("Available");
            seats.add(a); seats.add(b); seats.add(c); seats.add(d);
        }

        request.setAttribute("flightNo", flightNo);
        request.setAttribute("flightDate", "2025-08-10"); // static for now
        request.setAttribute("seats", seats);
        request.getRequestDispatcher("/WEB-INF/seat-selection.jsp").forward(request, response);
    }
}
