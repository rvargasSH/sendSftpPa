package sainthonore.api.sendFtpPa.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class CreateExcel {

    public String generateSellsExport(List<Map<String, Object>> detalleExcelViewModel) {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Services Sells");

            Row row = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // Creating header
            Cell cell = row.createCell(0);
            cell.setCellValue("Código Tienda");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(1);
            cell.setCellValue("Transacción");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(2);
            cell.setCellValue("Tienda");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(3);
            cell.setCellValue("Precio");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(4);
            cell.setCellValue("Cantidad");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(5);
            cell.setCellValue("Fecha Venta");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(6);
            cell.setCellValue("Vendedor");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(7);
            cell.setCellValue("Codigo SAP");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(8);
            cell.setCellValue("Nombre Producto");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(9);
            cell.setCellValue("Marca");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(10);
            cell.setCellValue("Familia");
            cell.setCellStyle(headerCellStyle);

            cell = row.createCell(11);
            cell.setCellValue("Grupo Producto");
            cell.setCellStyle(headerCellStyle);

            Integer i = 0;
            for (final Map sell : detalleExcelViewModel) {

                Row dataRow = sheet.createRow(i + 1);
                dataRow.createCell(0).setCellValue((String) sell.get("storeCode"));
                BigDecimal Idtransaction = (BigDecimal) sell.get("Idtransaction");
                dataRow.createCell(1).setCellValue(Idtransaction.toString());
                dataRow.createCell(2).setCellValue((String) sell.get("STORE_NAME"));
                dataRow.createCell(3).setCellValue((String) sell.get("price"));
                BigDecimal quantity = (BigDecimal) sell.get("quantity");
                dataRow.createCell(4).setCellValue(quantity.toString());
                dataRow.createCell(5).setCellValue((String) sell.get("sellDate"));
                dataRow.createCell(6).setCellValue((String) sell.get("seller"));
                dataRow.createCell(7).setCellValue((String) sell.get("productCode"));
                dataRow.createCell(8).setCellValue((String) sell.get("productName"));
                dataRow.createCell(9).setCellValue((String) sell.get("brand"));
                dataRow.createCell(10).setCellValue((String) sell.get("family"));
                dataRow.createCell(11).setCellValue((String) sell.get("productGroup"));

                i++;
            }

            for (i = 0; i < 11; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            FileOutputStream fos = new FileOutputStream(new File("servicessells.xlsx"));
            outputStream.writeTo(fos);
            fos.close();
            return "servicessells.xlsx";

        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return null;
        }

    }
}
