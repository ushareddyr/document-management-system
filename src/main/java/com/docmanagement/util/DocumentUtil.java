package com.docmanagement.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DocumentUtil {

    private static JdbcTemplate jdbcTemplate;

    @Autowired
    public DocumentUtil(JdbcTemplate template) {
        DocumentUtil.jdbcTemplate = template;
    }

    public static void updateTsvector(Long documentId, String content) {
        String sql = "UPDATE documents SET content_vector = to_tsvector('english', ?) WHERE id = ?";
        jdbcTemplate.update(sql, content, documentId);
    }
}
