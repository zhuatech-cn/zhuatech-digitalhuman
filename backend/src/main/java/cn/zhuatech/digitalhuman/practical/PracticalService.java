/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.digitalhuman.practical;

import static cn.zhuatech.digitalhuman.practical.PracticalInput.*;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class PracticalService {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  public Map<String, Object> execute(Map<String, Object> r) {
    validate(r);
    keys(r, "leadMs", "gapMs", "maxCodePointsPerSecond", "segments");
    int lead = integer(r, "leadMs", 0, 60000), gap = integer(r, "gapMs", 0, 60000);
    double max = decimal(r, "maxCodePointsPerSecond", 0.1, 100).doubleValue();
    long cursor = lead;
    int warnings = 0;
    List<Map<String, Object>> out = new ArrayList<>();
    for (var s : rows(r, "segments", 1, 300)) {
      keys(s, "id", "durationMs", "text");
      String id = id(s, "id");
      if (out.stream().anyMatch(x -> x.get("id").equals(id)))
        throw new IllegalArgumentException("段落编号重复");
      int duration = integer(s, "durationMs", 1, 600000);
      String text = text(s, "text", 2000);
      long count = text.codePoints().filter(c -> !Character.isWhitespace(c)).count();
      double cps = count * 1000.0 / duration;
      long start = cursor, end = start + duration;
      if (end > 86400000) throw new IllegalArgumentException("播报总时长不能超过24小时");
      boolean fast = cps > max;
      if (fast) warnings++;
      out.add(
          Map.of(
              "id",
              id,
              "startMs",
              start,
              "endMs",
              end,
              "codePointCount",
              count,
              "codePointsPerSecond",
              round(cps),
              "tooFast",
              fast));
      cursor = end + gap;
    }
    long total = cursor - gap;
    return Map.of(
        "segments",
        out,
        "totalDurationMs",
        total,
        "tooFastSegments",
        warnings,
        "audioGenerated",
        false);
  }
}
