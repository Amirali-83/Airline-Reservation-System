package com.example.airlinereservationsystem;

import java.sql.*;

public class SeatDao {

    public enum ReserveResult { RESERVED, ALREADY_TAKEN, NOT_FOUND }

    /**
     * Tries to reserve a seat by id. Succeeds only if it exists and is 'Available'.
     * Uses a single conditional UPDATE to avoid race conditions.
     */
    public ReserveResult reserve(int seatId, String passenger) throws SQLException {
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1) Attempt to mark seat as occupied only if it is currently Available
                String updateSql = """
                    UPDATE seats
                       SET status = 'Occupied'
                     WHERE seat_id = ?
                       AND status = 'Available'
                    """;
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setInt(1, seatId);
                    int updated = ps.executeUpdate();
                    if (updated == 1) {
                        con.commit();
                        return ReserveResult.RESERVED;
                    }
                }

                // 2) If no row updated, check whether the seat exists
                String existsSql = "SELECT status FROM seats WHERE seat_id = ?";
                try (PreparedStatement ps = con.prepareStatement(existsSql)) {
                    ps.setInt(1, seatId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return ReserveResult.NOT_FOUND;
                        }
                        // exists but not available
                        con.rollback();
                        return ReserveResult.ALREADY_TAKEN;
                    }
                }
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
