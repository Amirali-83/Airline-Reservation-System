package com.example.airlinereservationsystem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/book-flight")
public class BookingServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String flightNumber = request.getParameter("flightNumber");
        String seats = request.getParameter("seats");

        // Generate a random 6-digit booking ID
        int bookingId = (int) (Math.random() * 900000) + 100000;

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Flight Ticket</title></head><body>");
        out.println("<h2>Flight Ticket</h2>");
        out.println("<p><strong>Booking ID:</strong> #" + bookingId + "</p>");
        out.println("<p><strong>Name:</strong> " + name + "</p>");
        out.println("<p><strong>Email:</strong> " + email + "</p>");
        out.println("<p><strong>Flight Number:</strong> " + flightNumber + "</p>");
        out.println("<p><strong>Seats:</strong> " + seats + "</p>");
        out.println("<p><strong>Status:</strong> Confirmed ✅</p>");
        out.println("<br><a href='index.html'>Back to Home</a>");
        out.println("</body></html>");
    }
}
