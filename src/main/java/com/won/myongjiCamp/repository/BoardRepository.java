package com.won.myongjiCamp.repository;

import com.won.myongjiCamp.model.board.Board;
import com.won.myongjiCamp.model.board.RecruitBoard;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BoardRepository extends JpaRepository<Board,Long> {
}
