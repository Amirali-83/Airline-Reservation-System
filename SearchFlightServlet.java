package com.example.airlinereservationsystem;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@WebServlet("/search-flights")
public class SearchFlightServlet extends HttpServlet {
    private final FlightDao dao = new FlightDao();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String origin = req.getParameter("from");
        String destination = req.getParameter("to");
        String dateStr = req.getParameter("date");
        String cabin = (req.getParameter("cabin") == null ? "ANY" : req.getParameter("cabin")).toUpperCase();
        String paxStr = req.getParameter("pax");

        System.out.println("[Search] from=" + origin + " to=" + destination +
                " date=" + dateStr + " cabin=" + cabin + " pax=" + paxStr);

        if (origin == null || destination == null || dateStr == null || paxStr == null) {
            resp.sendError(400, "Missing required parameters");
            return;
        }

        int pax = Integer.parseInt(paxStr);
        java.time.LocalDate date = java.time.LocalDate.parse(dateStr); // HTML date format yyyy-MM-dd

        var flights = dao.search(origin, destination, date, cabin, pax);

        req.setAttribute("flights", flights);
        req.setAttribute("origin", origin.toUpperCase());
        req.setAttribute("destination", destination.toUpperCase());
        req.setAttribute("date", dateStr);
        req.setAttribute("pax", pax);

        req.getRequestDispatcher("/search-results.jsp").forward(req, resp);
    }
}
