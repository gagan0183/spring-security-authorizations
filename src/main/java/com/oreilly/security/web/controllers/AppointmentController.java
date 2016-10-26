package com.oreilly.security.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oreilly.security.domain.entities.Appointment;
import com.oreilly.security.domain.entities.AutoUser;
import com.oreilly.security.domain.repositories.AppointmentRepository;
import com.oreilly.security.domain.repositories.AppointmentUtils;

@Controller()
@RequestMapping("/appointments")
public class AppointmentController {

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private AppointmentUtils util;

	@ModelAttribute
	public Appointment getAppointment() {
		return new Appointment();
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getAppointmentPage() {
		return "appointments";
	}

	@ModelAttribute("isUser")
	public boolean isUser(Authentication authentication) {
		return (authentication != null)
				&& (authentication.getAuthorities().contains(AuthorityUtils.createAuthorityList("ROLE_USER").get(0)));
	}

	@ResponseBody
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public List<Appointment> saveAppointment(@ModelAttribute Appointment appointment) {
		AutoUser user = new AutoUser();
		user.setEmail("test@email.com");
		user.setFirstName("Joe");
		user.setLastName("Doe");
		appointment.setUser(user);
		appointment.setStatus("Initial");
		appointmentRepository.save(appointment);
		return this.appointmentRepository.findAll();
	}

	@ResponseBody
	@RequestMapping("/all")
	public List<Appointment> getAppointments(Authentication authentication) {
		return this.appointmentRepository.findByUser((AutoUser) authentication.getPrincipal());
	}

	@ResponseBody
	@RequestMapping("/test")
	public String test(Authentication authentication) {
		AutoUser autoUser = (AutoUser) authentication.getPrincipal();
		AutoUser otherUser = new AutoUser();
		otherUser.setEmail("ar@ar.com");
		return util.saveAll(new ArrayList<Appointment>() {
			{
				add(util.createAppointment(autoUser));
				add(util.createAppointment(otherUser));
			}
		});
	}

	@RequestMapping("/{appointmentId}")
	// @PostAuthorize(value = "principal.autoUserId ==
	// #model[appointment].user.autoUserId")
	@PostAuthorize(value = "returnObject == 'appointment'")
	public String getAppointment(@PathVariable("appointmentId") Long appointmentId, Model model) {
		Appointment appointment = appointmentRepository.findOne(appointmentId);
		model.addAttribute("appointment", appointment);
		return "appointment";
	}

	@ResponseBody
	@RequestMapping("/confirm")
	@RolesAllowed(value = "ROLE_ADMIN")
	public String confirm() {
		return "confirmed";
	}

	@ResponseBody
	@RequestMapping("/cancel")
	public String cancel() {
		return "cancelled";
	}

	@ResponseBody
	@RequestMapping("/complete")
	@RolesAllowed(value = "ROLE_ADMIN")
	public String complete() {
		return "complete";
	}
}
