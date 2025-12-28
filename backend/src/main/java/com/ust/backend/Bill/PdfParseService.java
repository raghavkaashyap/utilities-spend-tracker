package com.ust.backend.bill;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class PdfParseService {

    public String extractText(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(-1); // unlimited
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            // Use PDF parser explicitly to ensure PDF capabilities are loaded
            PDFParser pdfParser = new PDFParser();
            context.set(org.apache.tika.parser.Parser.class, pdfParser);
            pdfParser.parse(is, handler, metadata, context);
            return handler.toString();
        }
    }

    public String extractText(InputStream is) throws Exception {
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        PDFParser pdfParser = new PDFParser();
        context.set(org.apache.tika.parser.Parser.class, pdfParser);
        pdfParser.parse(is, handler, metadata, context);
        return handler.toString();
    }
}
