package com.hamstercoders.netty.status;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class Dao {

    private DbConnection connect;

    public Dao() {
        DbConnection connection = new DbConnection("localhost:3306", "root", "admin", "connections");
        connection.initProperties();
        connection.init();
        setConnect(connection);
    }

    public DbConnection getConnect() {
        return connect;
    }

    public void setConnect(DbConnection connect) {
        this.connect = connect;
    }

    public void mergeServerRequestStatus(ServerRequestStatus record) {
        StringBuilder stb = new StringBuilder("");
        stb.append("insert into request(src_ip, url, timestamp, redirect) values(")
                .append("'")
                .append(record.getSrcIp())
                .append("'")
                .append(", ")
                .append("null")
                .append(", ")
                .append("'")
                .append(new Timestamp(Calendar.getInstance().getTimeInMillis()))
                .append("'")
                .append(", ")
                .append(0)
                .append(")");
        connect.updateQuery(stb.toString());
    }

    public List<ServerRequestStatus> getServerRequest() throws SQLException {
        List<ServerRequestStatus> resultList = new ArrayList<>();

        ResultSet rs = connect.query("select src_ip, timestamp from request");

        String ip = "";
        Timestamp ts = null;
        boolean ex = false;

        while (rs.next()) {
            ip = rs.getString("src_ip");
            ts = rs.getTimestamp("timestamp");
            ex = false;
            for (int i = 0; i < resultList.size(); i++) {
                if (ip.equals(resultList.get(i).getSrcIp())) {
                    resultList.get(i).incRequestCount();
                    ex = true;
                    if (resultList.get(i).getLastRequest().compareTo(ts) < 0) {
                        resultList.get(i).setLastRequest(ts);
                    }
                }
            }
            if (!ex) {
                resultList.add(new ServerRequestStatus(ip, ts, 1));
            }
        }
        return resultList;
    }

    public long getServerRequestCount() throws SQLException {
        ResultSet rs = connect.query("select count(src_ip) as total from request");
        rs.next();
        return rs.getLong("total");
    }

    public long getServerRequestUniqueCount() throws SQLException {
        ResultSet rs = connect.query("select count(distinct src_ip) as total from request");
        rs.next();
        return rs.getLong("total");
    }

    public void mergeRedirectRequestStatus(RedirectRequestStatus record) throws SQLException {
        StringBuilder stb = new StringBuilder("");
        stb.append("insert into request(src_ip, url, timestamp, redirect) values(")
                .append("'")
                .append(record.getSrcIp())
                .append("'")
                .append(", ")
                .append("'")
                .append(record.getRedirectUrl())
                .append("'")
                .append(", ")
                .append("'")
                .append(new Timestamp(Calendar.getInstance().getTimeInMillis()))
                .append("'")
                .append(", ")
                .append(1)
                .append(")");
        connect.updateQuery(stb.toString());
    }

    public Map<String, Long> getRedirectRequest() throws SQLException {
        Map<String, Long> result = new HashMap<>();
        ResultSet rs = connect.query("select url from request where url is not null");
        String url = "";

        while (rs.next()) {
            url = rs.getString("url");
            if (result.containsKey(url)) {
                result.put(url, result.get(url) + 1);
            } else {
                result.put(url, 1L);
            }
        }
        return result;
    }

    public void shutdownDb() throws SQLException {
        connect.close();
    }

}
