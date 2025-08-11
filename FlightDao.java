package com.example.airlinereservationsystem;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;

public class FlightDao {

    public List<Flight> search(String origin, String destination, LocalDate date, String cabin, int pax) {
        boolean anyCabin = cabin == null || cabin.equalsIgnoreCase("ANY");

        String sql = """
        SELECT * FROM flights
        WHERE origin = ? AND destination = ?
          AND DATE(depart_time) = ?
          AND seats_left >= ?
    """ + (anyCabin ? "" : " AND UPPER(cabin) = UPPER(?) ") +
                " ORDER BY depart_time";

        List<Flight> result = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, origin.toUpperCase());
            ps.setString(i++, destination.toUpperCase());
            ps.setDate(i++, java.sql.Date.valueOf(date));
            ps.setInt(i++, pax);
            if (!anyCabin) ps.setString(i++, cabin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Flight f = new Flight();
                    f.setId(rs.getLong("id"));
                    f.setFlightNo(rs.getString("flight_no"));
                    f.setOrigin(rs.getString("origin"));
                    f.setDestination(rs.getString("destination"));
                    f.setDepartTime(rs.getTimestamp("depart_time").toLocalDateTime());
                    f.setArriveTime(rs.getTimestamp("arrive_time").toLocalDateTime());
                    f.setDurationMinutes(rs.getInt("duration_minutes"));
                    f.setCabin(rs.getString("cabin"));
                    f.setPrice(rs.getBigDecimal("price"));
                    f.setSeatsLeft(rs.getInt("seats_left"));
                    result.add(f);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Search failed", e);
        }
        return result;
    }
}