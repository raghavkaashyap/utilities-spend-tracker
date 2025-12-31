package com.ust.backend.bill;

/**
 * Parses raw PDF-extracted text into structured bill fields.
 * Implementation must be best-effort: return whatever can be confidently extracted
 * and leave other fields null. Parsing must not throw for missing fields.
 */
public interface BillParser {
    ParsedBill parse(String text, String sourceFilename);
}
