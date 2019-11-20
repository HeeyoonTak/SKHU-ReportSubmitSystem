package com.report.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.report.dto.Homework;
import com.report.dto.Lecture;
import com.report.dto.Lecturefile;
import com.report.dto.Professor;
import com.report.dto.ProfessorNotice;
import com.report.dto.StudentNotice;
import com.report.dto.Ta;
import com.report.mapper.HomeworkMapper;
import com.report.mapper.LectureMapper;
import com.report.mapper.LecturefileMapper;
import com.report.mapper.ProfessorMapper;
import com.report.mapper.ProfessorNoticeMapper;
import com.report.mapper.StudentNoticeMapper;
import com.report.mapper.TaMapper;
import com.report.model.Pagination;
import com.report.service.LecturefileService;
import com.report.service.StudentNoticeService;

@Controller

@RequestMapping("ta")
public class TaController {

	@Autowired TaMapper taMapper;
	@Autowired LectureMapper lectureMapper;
	@Autowired ProfessorMapper professorMapper;
	@Autowired ProfessorNoticeMapper professorNoticeMapper;
	@Autowired HomeworkMapper homeworkMapper;
	@Autowired LecturefileMapper lecturefileMapper;
	@Autowired LecturefileService lecturefileService;
	@Autowired StudentNoticeService studentNoticeService;
	@Autowired StudentNoticeMapper studentNoticeMapper;

	@RequestMapping("taMain")
	public String taMain(Model model, Principal principal) {
		Ta ta = taMapper.findByTaId(principal.getName());
		Professor professor = professorMapper.findByProfessorId(principal.getName());
		List<Lecture> taLecture = lectureMapper.findByTaLecture(ta.getTa_no());

		model.addAttribute("ta", ta);
		model.addAttribute("professor", professor);
		model.addAttribute("taLecture", taLecture);


		return "ta/main"; // 로그인 한 ta를 위한 메인 페이지 URL
	}

	@RequestMapping("information")
	public String information(Model model, Principal principal) {
		Ta ta = taMapper.findByTaId(principal.getName());
		model.addAttribute("ta", ta);
		return "ta/information"; // 로그인 한 ta를 위한 메인 페이지 URL
	}

	@RequestMapping("studentnotice")
	public String studentnotice(Model model,Principal principal, @RequestParam("id") int id, Pagination pagination) {
		Ta ta = taMapper.findByTaId(principal.getName());
		Lecture lecture = lectureMapper.findOne(id);
		List<StudentNotice> studentNotices = studentNoticeService.listWithStudentName(id,pagination);
		pagination.setRecordCount(lecturefileMapper.count(id));
		model.addAttribute("lecture", lecture);
		model.addAttribute("ta", ta);
		model.addAttribute("studentNotices", studentNotices);
		return "ta/studentnotice"; // 학생 게시판 페이지
	}

	@GetMapping("studentcontent")
	public String studentcontent(Model model, Principal principal,
								 @RequestParam("id") int id ){
		Ta ta = taMapper.findByTaId(principal.getName());
		StudentNotice studentNotice = studentNoticeMapper.findOne(id);
        Lecture lecture = lectureMapper.findOne(studentNotice.getLecture_no());

//		System.out.println(studentNotice.getStudent_no());
//		System.out.println(principal.getName());

		model.addAttribute("lecture", lecture);
		model.addAttribute("ta", ta);
		model.addAttribute("studentNotice", studentNotice);

		return "ta/studentcontent"; // 학생 게시판 페이지
	}

	@RequestMapping("mypage")
	public String mypage(Model model, Principal principal) {
		Ta ta = taMapper.findByTaId(principal.getName());
		model.addAttribute("ta", ta);
		return "ta/mypage"; // 로그인 한 ta를 위한 메인 페이지 URL
	}

	@RequestMapping("notice")
	public String notice(Model model, Principal principal, @RequestParam("id") int id,Pagination pagination) {
		Ta ta = taMapper.findByTaId(principal.getName());
		model.addAttribute("ta", ta);
		Lecture lecture = lectureMapper.findOne(id);
		List<ProfessorNotice>  professorNotices = professorNoticeMapper.list(id, pagination);
		pagination.setRecordCount(professorNoticeMapper.count(id));
		model.addAttribute("lecture", lecture);
		model.addAttribute("professorNotices", professorNotices);
		return "ta/notice"; // 과제 및 공지 페이지
	}

	@RequestMapping("lecturefile")
	public String lecturefile(Model model, Principal principal, @RequestParam("id") int id, Pagination pagination) {
		Ta ta = taMapper.findByTaId(principal.getName());
		Lecture lecture = lectureMapper.findOne(id);
		List<Lecturefile> lecturefiles = lecturefileService.findAll(id,pagination);
		pagination.setRecordCount(lecturefileMapper.count(id));
		model.addAttribute("lecture", lecture);
		model.addAttribute("ta", ta);
		model.addAttribute("files", lecturefiles); // 업로드된 파일리스트
		return "ta/lecturefile"; // 강의자료 페이지
	}

	@GetMapping("noticecontent")
	public String noticecontent(Model model,Principal principal, @RequestParam("id") int id) {
		Ta ta = taMapper.findByTaId(principal.getName());
		ProfessorNotice professorNoice = professorNoticeMapper.findOne(id);
		Lecture lecture = lectureMapper.findOne(professorNoice.getLecture_no());
		model.addAttribute("lecture", lecture);
		model.addAttribute("ta", ta);
		model.addAttribute("professorNotice", professorNoice);
		return "ta/noticecontent"; // 과제 및 공지 작성 페이지
	}


	@RequestMapping(value="inputscore", method=RequestMethod.GET)
	public String inputscore1(Model model, Principal principal, @RequestParam("notice_no") int notice_no) {
		// id notice_no를 받아와야함.... 지금 임의의 값을 주고 있음
		List<Homework> homeworks = homeworkMapper.findNotoiceStudents(notice_no);
		model.addAttribute("homeworks", homeworks);

		return "professor/inputscore";
	}



	@RequestMapping(value="inputscore", method=RequestMethod.POST, params="cmd=input")
	public String inputscore2(Model model,
			@RequestParam("notice_no") int notice_no,
			@RequestParam("hw_no") int[] hw_no,
			@RequestParam("grade") int[] grade,
			@RequestParam("ranking") int[] ranking) {




		List<Homework> homeworks = homeworkMapper.findNotoiceStudents(notice_no);

		for (int i=0; i < hw_no.length ;++i) {
			System.out.println("======================");
			homeworkMapper.gradeUpdate(grade[i], ranking[i],hw_no[i]);
			System.out.printf("점수 : %d,	등수 : %d, 과제번호 :%d\n",grade[i], ranking[i], hw_no[i]);
			System.out.println("======================\n\n");
		}


		model.addAttribute("homeworks", homeworks);
		return "redirect:inputscore?notice_no="+notice_no; // 학생 게시판 페이지
	}
}
