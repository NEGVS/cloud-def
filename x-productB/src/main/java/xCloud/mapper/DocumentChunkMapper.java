package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xCloud.entity.recruitment.DocumentChunk;

import java.util.List;

/**
 * 招聘文档块 Mapper，支持关键词全文检索
 */
@Mapper
public interface DocumentChunkMapper extends BaseMapper<DocumentChunk> {

    /**
     * 关键词检索（MySQL LIKE，用于 Hybrid RAG 的关键词路）
     * 多关键词用空格分隔，逐词 OR 匹配
     */
    @Select("<script>" +
            "SELECT * FROM recruitment_chunk WHERE " +
            "<foreach collection='keywords' item='kw' separator=' OR '>" +
            "content LIKE CONCAT('%', #{kw}, '%')" +
            "</foreach>" +
            " ORDER BY create_time DESC LIMIT #{limit}" +
            "</script>")
    List<DocumentChunk> keywordSearch(
            @Param("keywords") List<String> keywords,
            @Param("limit") int limit
    );

    /**
     * 真正的批量 insert（单条 SQL 多 VALUES），比逐条 insert 减少 N-1 次网络往返
     */
    @Insert("<script>" +
            "INSERT INTO recruitment_chunk (id, content, source, doc_type, chunk_index, page_num, create_time) VALUES " +
            "<foreach collection='list' item='c' separator=','>" +
            "(#{c.id}, #{c.content}, #{c.source}, #{c.docType}, #{c.chunkIndex}, #{c.pageNum}, #{c.createTime})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<DocumentChunk> list);

    /**
     * 按 id 批量查询（向量检索后回查文本）
     */
    @Select("<script>" +
            "SELECT * FROM recruitment_chunk WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<DocumentChunk> selectByIds(@Param("ids") List<Long> ids);
}
