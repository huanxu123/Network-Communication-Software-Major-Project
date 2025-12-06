package com.example.admin.repository;

import com.example.admin.entity.CallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {
    // 查找跟某人有关的所有通话（无论是他打的，还是接的）
    List<CallRecord> findByCallerOrCalleeOrderByStartTimeDesc(String caller, String callee);
}
