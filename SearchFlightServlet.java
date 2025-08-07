package com.example.airlinereservationsystem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/SearchFlightServlet")
public class SearchFlightServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get parameters from form
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String date = request.getParameter("date");
        String passengers = request.getParameter("passengers");
        String[] travelClass = request.getParameterValues("class");

        // Set form data
        request.setAttribute("from", from);
        request.setAttribute("to", to);
        request.setAttribute("date", date);
        request.setAttribute("passengers", passengers);
        request.setAttribute("travelClass", String.join(", ", travelClass));

        // Create mock data (instead of querying DB)
        List<String> flightResults = new ArrayList<>();
        flightResults.add("Flight 101 | Berlin ✈ London | 09:00 - 11:00");
        flightResults.add("Flight 202 | Berlin ✈ London | 14:30 - 16:20");
        flightResults.add("Flight 303 | Berlin ✈ London | 20:45 - 22:15");

        // Attach to request
        request.setAttribute("flightResults", flightResults);

        // Forward to JSP
        request.getRequestDispatcher("search-results.jsp").forward(request, response);
    }
}
