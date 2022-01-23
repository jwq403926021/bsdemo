package com.orangeforms.common.core.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.sax.handler.RowHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.orangeforms.common.core.exception.MyRuntimeException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 导入工具类，目前支持xlsx和xls两种类型。
 *
 * @author Jerry
 * @date 2021-06-06
 */
@Slf4j
public class ImportUtil {

    /**
     * 根据实体类的Class类型，生成导入的头信息。
     *
     * @param modelClazz   实体对象的Class类型。
     * @param ignoreFields 忽略的字段名集合，如创建时间、创建人、更新时间、更新人等。
     * @param <T> 实体对象类型。
     * @return 创建后的导入头信息列表。
     */
    public static <T> List<ImportHeaderInfo> makeHeaderInfoList(Class<T> modelClazz, Set<String> ignoreFields) {
        List<ImportHeaderInfo> resultList = new LinkedList<>();
        Field[] fields = ReflectUtil.getFields(modelClazz);
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            // transient类型的字段不能作为查询条件，静态字段和逻辑删除都不考虑。需要忽略的字段也要跳过。
            int transientMask = 128;
            if ((modifiers & transientMask) == 1
                    || Modifier.isStatic(modifiers)
                    || field.getAnnotation(TableLogic.class) != null
                    || ignoreFields.contains(field.getName())) {
                continue;
            }
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField == null || tableField.exist()) {
                ImportHeaderInfo headerInfo = new ImportHeaderInfo();
                headerInfo.fieldName = field.getName();
                if (field.getType().equals(Integer.class)) {
                    headerInfo.fieldType = INT_TYPE;
                } else if (field.getType().equals(Long.class)) {
                    headerInfo.fieldType = LONG_TYPE;
                } else if (field.getType().equals(String.class)) {
                    headerInfo.fieldType = STRING_TYPE;
                } else if (field.getType().equals(Boolean.class)) {
                    headerInfo.fieldType = BOOLEAN_TYPE;
                } else if (field.getType().equals(Date.class)) {
                    headerInfo.fieldType = DATE_TYPE;
                } else if (field.getType().equals(Double.class)) {
                    headerInfo.fieldType = DOUBLE_TYPE;
                } else if (field.getType().equals(Float.class)) {
                    headerInfo.fieldType = FLOAT_TYPE;
                } else if (field.getType().equals(BigDecimal.class)) {
                    headerInfo.fieldType = BIG_DECIMAL_TYPE;
                } else {
                    throw new MyRuntimeException("Unsupport Import FieldType");
                }
                resultList.add(headerInfo);
            }
        }
        return resultList;
    }

    /**
     * 保存导入文件。
     *
     * @param baseDir    导入文件本地缓存的根目录。
     * @param subDir     导入文件本地缓存的子目录。
     * @param importFile 导入的文件。
     * @return 保存的本地文件名。
     */
    public static String saveImportFile(
            String baseDir, String subDir, MultipartFile importFile) throws IOException {
        StringBuilder sb = new StringBuilder(256);
        sb.append(baseDir);
        if (!StrUtil.endWith(baseDir, "/")) {
            sb.append("/");
        }
        sb.append("importedFile/");
        if (StrUtil.isNotBlank(subDir)) {
            sb.append(subDir);
            if (!StrUtil.endWith(subDir, "/")) {
                sb.append("/");
            }
        }
        String pathname = sb.toString();
        sb.append(new DateTime().toString("yyyy-MM-dd-HH-mm-"));
        sb.append(MyCommonUtil.generateUuid())
                .append(".").append(FileNameUtil.getSuffix(importFile.getOriginalFilename()));
        String fullname = sb.toString();
        try {
            byte[] bytes = importFile.getBytes();
            Path path = Paths.get(fullname);
            // 如果没有files文件夹，则创建
            if (!Files.isWritable(path)) {
                Files.createDirectories(Paths.get(pathname));
            }
            // 文件写入指定路径
            Files.write(path, bytes);
        } catch (IOException e) {
            log.error("Failed to write imported file [" + importFile.getOriginalFilename() + " ].", e);
            throw e;
        }
        return fullname;
    }

    /**
     * 导入指定的excel，基于SAX方式解析后返回数据列表。
     *
     * @param headers    头信息数组。
     * @param skipHeader 是否跳过第一行，通常改行为头信息。
     * @param filename   文件名。
     * @return 解析后数据列表。
     */
    public static List<Map<String, Object>> doImport(
            ImportHeaderInfo[] headers, boolean skipHeader, String filename) {
        Assert.notNull(headers);
        Assert.isTrue(StrUtil.isNotBlank(filename));
        List<Map<String, Object>> resultList = new LinkedList<>();
        ExcelUtil.readBySax(new File(filename), 0, createRowHandler(headers, skipHeader, resultList));
        return resultList;
    }

    /**
     * 导入指定的excel，基于SAX方式解析后返回Bean类型的数据列表。
     *
     * @param headers    头信息数组。
     * @param skipHeader 是否跳过第一行，通常改行为头信息。
     * @param filename   文件名。
     * @param clazz      Bean的Class类型。
     * @return 解析后数据列表。
     */
    public static <T> List<T> doImport(
            ImportHeaderInfo[] headers, boolean skipHeader, String filename, Class<T> clazz) {
        List<Map<String, Object>> resultList = doImport(headers, skipHeader, filename);
        return MyModelUtil.mapToBeanList(resultList, clazz);
    }

    private static RowHandler createRowHandler(
            ImportHeaderInfo[] headers, boolean skipHeader, List<Map<String, Object>> resultList) {
        return new MyRowHandler(headers, skipHeader, resultList);
    }

    public final static int INT_TYPE = 0;
    public final static int LONG_TYPE = 1;
    public final static int STRING_TYPE = 2;
    public final static int BOOLEAN_TYPE = 3;
    public final static int DATE_TYPE = 4;
    public final static int DOUBLE_TYPE = 5;
    public final static int FLOAT_TYPE = 6;
    public final static int BIG_DECIMAL_TYPE = 7;

    @Data
    public static class ImportHeaderInfo {
        /**
         * 对应的Java实体对象属性名。
         */
        private String fieldName;
        /**
         * 对应的Java实体对象类型。
         */
        private Integer fieldType;
    }

    private static class MyRowHandler implements RowHandler {

        private ImportHeaderInfo[] headers;
        private boolean skipHeader;
        private List<Map<String, Object>> resultList;

        public MyRowHandler(ImportHeaderInfo[] headers, boolean skipHeader, List<Map<String, Object>> resultList) {
            this.headers = headers;
            this.skipHeader = skipHeader;
            this.resultList = resultList;
        }

        @Override
        public void handle(int sheetIndex, long rowIndex, List<Object> rowList) {
            if (this.skipHeader && rowIndex == 0) {
                return;
            }
            int i = 0;
            Map<String, Object> data = new HashMap<>(headers.length);
            for (Object rowData : rowList) {
                if (i >= headers.length) {
                    log.warn("Exceeded the size of headers and ignore the left columns");
                    break;
                }
                ImportHeaderInfo headerInfo = this.headers[i++];
                switch (headerInfo.fieldType) {
                    case INT_TYPE:
                        data.put(headerInfo.fieldName, Convert.toInt(rowData));
                        break;
                    case LONG_TYPE:
                        data.put(headerInfo.fieldName, Convert.toLong(rowData));
                        break;
                    case STRING_TYPE:
                        data.put(headerInfo.fieldName, Convert.toStr(rowData));
                        break;
                    case BOOLEAN_TYPE:
                        data.put(headerInfo.fieldName, Convert.toBool(rowData));
                        break;
                    case DATE_TYPE:
                        data.put(headerInfo.fieldName, Convert.toDate(rowData));
                        break;
                    case DOUBLE_TYPE:
                        data.put(headerInfo.fieldName, Convert.toDouble(rowData));
                        break;
                    case FLOAT_TYPE:
                        data.put(headerInfo.fieldName, Convert.toFloat(rowData));
                        break;
                    case BIG_DECIMAL_TYPE:
                        data.put(headerInfo.fieldName, Convert.toBigDecimal(rowData));
                        break;
                    default:
                        throw new MyRuntimeException(
                                "Invalid ImportHeaderInfo.fieldType [" + headerInfo.fieldType + "].");
                }
            }
            resultList.add(data);
        }
    }

    /**
     * 私有构造函数，明确标识该常量类的作用。
     */
    private ImportUtil() {
    }
}
