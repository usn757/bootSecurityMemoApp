package org.example.bootsecurity.service;

import org.example.bootsecurity.model.domain.Memo;
import org.example.bootsecurity.model.mapper.MemoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoServiceImpl implements MemoService {
    private final MemoMapper memoMapper;

    public MemoServiceImpl(MemoMapper memoMapper) {
        this.memoMapper = memoMapper;
    }

    @Override
    public List<Memo> findAll() {
        return memoMapper.findAll();
    }

    @Override
    public void create(Memo memo) throws Exception {
        memoMapper.insert(memo);
    }

    @Override
    public void delete(Long id) throws Exception {
        memoMapper.delete(id);
    }

    @Override
    public void deleteAll() {
        memoMapper.deleteAll();
    }

    @Override
    public Memo findById(Long id) {
        return memoMapper.findById(id);
    }

    @Override
    public void update(Memo newMemo) {
        memoMapper.update(newMemo);
    }
}