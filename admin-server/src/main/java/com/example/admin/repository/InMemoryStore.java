package com.example.admin.repository;

import com.example.admin.entity.CallRecord;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class InMemoryStore {

    // 队友原本想在这里造假数据，但现在我们有真数据库了，
    // 这些假数据格式不对会报错，所以我们直接把它们变成“空列表”。

    // 如果这一行报错找不到 UserSummary，你可以把它整行删掉，通常没影响
    private final List<Object> users = Collections.emptyList();

    // 这里的 CallRecord 假数据和你的真图纸冲突了，直接清空
    private final List<CallRecord> calls = Collections.emptyList();

    public List<Object> users() {
        return users;
    }

    public List<CallRecord> calls() {
        return calls;
    }
}