package com.agileoracleseval.slitheringeval.alharithAlkindi2.RMS.src.main.java.HRRecruitmentManagementSystem.HRRecruitmentManagementSystem;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HrRecruitmentManagementSystemApplication implements CommandLineRunner {

	private final NotificationDrainer drainer;

	public HrRecruitmentManagementSystemApplication(NotificationDrainer drainer) {
		this.drainer = drainer;
	}

	public static void main(String[] args) {
		SpringApplication.run(HrRecruitmentManagementSystemApplication.class, args);
	}

	@Override
	public void run(String... args) {
		int sent = drainer.drain();
		System.out.println("Drain complete. " + sent + " notification(s) posted and marked SENT.");
	}
}