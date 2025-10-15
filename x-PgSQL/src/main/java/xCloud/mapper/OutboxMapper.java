package xCloud.mapper;

import xCloud.entity.OutboxEvent;

import java.util.List;

public interface OutboxMapper {
    List<OutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();
}
