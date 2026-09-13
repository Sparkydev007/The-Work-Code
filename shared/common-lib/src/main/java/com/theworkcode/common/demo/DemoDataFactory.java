package com.theworkcode.common.demo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic synthetic data generator for The Work Code demo dataset.
 * Produces internally consistent fictional employers, employees, employment
 * history and payroll that satisfies the data consistency rules:
 *
 * <ul>
 *   <li>Termination date never precedes hire date.</li>
 *   <li>Historical tenures never overlap.</li>
 *   <li>Annualized income is consistent with monthly base income.</li>
 *   <li>Pay periods align with pay frequency and employment window.</li>
 * </ul>
 *
 * <p>All people and companies are fictional; any resemblance to real records
 * is coincidental. Never load real employee data into this platform.
 */
public final class DemoDataFactory {

    /** Deterministic scenario employee numbers used by the interview launcher. */
    public static final String SCENARIO_PERFECT = "DEMO-PERFECT-001";
    public static final String SCENARIO_MISMATCH = "DEMO-MISMATCH-001";
    public static final String SCENARIO_NOHIT = "DEMO-NOHIT-001";
    public static final String SCENARIO_INCOME = "DEMO-INCOME-001";
    public static final String SCENARIO_FRAUD = "DEMO-FRAUD-001";
    public static final String SCENARIO_MANUAL = "DEMO-MANUAL-001";

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 1);

    private static final List<String> FIRST_NAMES = List.of(
            "Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun", "Sai", "Reyansh", "Krishna", "Ishaan", "Rohan",
            "Priya", "Ananya", "Diya", "Aadhya", "Myra", "Sara", "Ishita", "Kavya", "Meera", "Nisha",
            "Rahul", "Vikram", "Suresh", "Anita", "Deepak", "Farhan", "Gauri", "Harini", "Imran", "Jyoti",
            "Karan", "Lakshmi", "Mohan", "Neha", "Omkar", "Pooja", "Qadir", "Ritu", "Sanjay", "Tara",
            "Alex", "Elena", "David", "Maria", "James", "Chen", "Yuki", "Omar", "Fatima", "Lucas");

    private static final List<String> LAST_NAMES = List.of(
            "Sharma", "Verma", "Iyer", "Patel", "Reddy", "Nair", "Gupta", "Mehta", "Kulkarni", "Joshi",
            "Banerjee", "Chatterjee", "Das", "Fernandes", "Ghosh", "Hegde", "Jain", "Khan", "Menon", "Pillai",
            "Rao", "Shetty", "Singh", "Trivedi", "Desai", "Bose", "Chauhan", "Dutta", "Kapoor", "Malhotra",
            "Walker", "Kim", "Tanaka", "Ali", "Novak", "Silva");

    private static final List<String> CITIES = List.of(
            "Bengaluru", "Mumbai", "Pune", "Hyderabad", "Chennai", "Delhi NCR", "Gurugram", "Kolkata",
            "Ahmedabad", "Jaipur", "Kochi", "Noida");

    private static final List<String> INDUSTRIES = List.of(
            "Information Technology", "Financial Services", "Healthcare", "Manufacturing", "Retail",
            "Logistics", "Consulting", "Telecom", "Energy", "Education");

    private static final List<String> DEPARTMENTS = List.of(
            "Engineering", "Product", "Finance", "Operations", "Sales", "Human Resources",
            "Marketing", "Customer Success", "Data & Analytics", "Quality Assurance");

    private static final List<String> TITLES = List.of(
            "Software Engineer", "Senior Software Engineer", "Lead Engineer", "Engineering Manager",
            "Product Analyst", "Financial Analyst", "Operations Executive", "Sales Manager",
            "HR Business Partner", "Marketing Specialist", "Data Analyst", "QA Engineer",
            "Associate Consultant", "Senior Consultant", "Account Manager");

    private static final String[] EMPLOYER_NAMES = {
            "Acme Technologies Pvt Ltd", "Nova Systems India", "BluePeak Financial", "Vertex Digital Labs",
            "Meridian Consulting", "Pioneer Mobility", "Aster Infotech", "Northstar Analytics",
            "Quantum Leap Solutions", "Silverline Software", "Trident Fintech", "Helios Healthtech",
            "Zenith Logistics", "Orbit Retail Group", "Cobalt Cloud Services", "Saffron Payroll Services",
            "Indus Analytics", "Vega Communications", "Marigold Manufacturing", "Kite Education Group"
    };

    private static final List<String> PAY_FREQUENCIES = List.of("WEEKLY", "BIWEEKLY", "SEMIMONTHLY", "MONTHLY");

    private DemoDataFactory() {
    }

    /** 20 deterministic fictional employers. */
    public static List<DemoEmployer> employers() {
        SeededRandom rnd = new SeededRandom(42L);
        List<DemoEmployer> list = new ArrayList<>(EMPLOYER_NAMES.length);
        for (int i = 0; i < EMPLOYER_NAMES.length; i++) {
            String name = EMPLOYER_NAMES[i];
            String city = CITIES.get((i * 7 + 3) % CITIES.size());
            String industry = INDUSTRIES.get((i * 3 + 1) % INDUSTRIES.size());
            int employeeCount = 40 + ((i * 137) % 1460);
            String trust = (i % 9 == 4) ? "PENDING_REVIEW" : "VERIFIED_CONTRIBUTOR";
            LocalDate since = LocalDate.of(2015 + (i % 9), 1 + (i % 12), 1 + (i % 27));
            LocalDate updated = TODAY.minusDays(i % 5);
            list.add(new DemoEmployer(employerCode(name), name, industry, city, employeeCount, trust, since, updated));
        }
        return list;
    }

    public static String employerCode(String employerName) {
        String slug = employerName.replaceAll("[^A-Za-z ]", "").trim().toUpperCase(Locale.ROOT);
        String[] words = slug.split("\\s+");
        StringBuilder sb = new StringBuilder("EMP-");
        for (String w : words) {
            if (sb.length() < 8 && !w.isBlank()) {
                sb.append(w.charAt(0));
            }
        }
        return sb.toString();
    }

    /**
     * Generates employeeCount deterministic employees across the employer
     * network, including the fixed scenario employees (perfect match, name
     * mismatch, income anomaly, duplicate identity, manual fallback).
     */
    public static List<DemoEmployee> employees(int employeeCount) {
        SeededRandom rnd = new SeededRandom(1337L);
        List<DemoEmployer> employers = employers();
        List<DemoEmployee> list = new ArrayList<>(employeeCount);

        list.add(scenarioPerfect());
        list.add(scenarioMismatch());
        list.add(scenarioIncomeAnomaly());
        list.add(scenarioDuplicate());
        list.add(scenarioManual());

        int serial = 1000;
        while (list.size() < employeeCount) {
            serial += 7;
            String number = "EMP-" + (48000 + serial);
            String first = pick(rnd, FIRST_NAMES);
            String last = pick(rnd, LAST_NAMES);
            String name = first + " " + last;
            DemoEmployer employer = pick(rnd, employers);
            LocalDate dob = LocalDate.of(1978 + rnd.nextInt(24), 1 + rnd.nextInt(12), 1 + rnd.nextInt(28));
            String emailDomain = emailDomain(employer.name());
            String email = (first + "." + last).toLowerCase(Locale.ROOT) + rnd.nextInt(90) + "@" + emailDomain;
            String phone = "+91 9" + String.format("%09d", rnd.between(100000000L, 999999999L));

            int roll = rnd.nextInt(100);
            String status;
            if (roll < 78) {
                status = DemoEmployee.STATUS_ACTIVE;
            } else if (roll < 86) {
                status = DemoEmployee.STATUS_TERMINATED;
            } else if (roll < 93) {
                status = DemoEmployee.STATUS_INACTIVE;
            } else {
                status = DemoEmployee.STATUS_ON_LEAVE;
            }

            LocalDate hire = LocalDate.of(2016 + rnd.nextInt(9), 1 + rnd.nextInt(12), 1 + rnd.nextInt(28));
            LocalDate termination = null;
            if (DemoEmployee.STATUS_TERMINATED.equals(status)) {
                int maxYears = Math.max(1, TODAY.getYear() - hire.getYear());
                termination = hire.plusDays(rnd.between(200, 365L * Math.min(3, maxYears) + 120));
                if (!termination.isBefore(TODAY.minusDays(10))) {
                    termination = TODAY.minusDays(rnd.between(30, 300));
                }
            }

            String dept = pick(rnd, DEPARTMENTS);
            String title = pick(rnd, TITLES);
            String employmentType = rnd.chance(82) ? "FULL_TIME" : (rnd.chance(60) ? "CONTRACT" : "PART_TIME");
            String location = employer.city() + " - " + rnd.pick("HQ Campus", "Tech Park", "Branch Office", "Remote");

            long monthlyBase = monthlyBase(rnd, title);
            String payFreq = rnd.chance(55) ? "MONTHLY" : PAY_FREQUENCIES.get(rnd.nextInt(3));

            list.add(new DemoEmployee(number, name, dob, email, phone, employer.name(), status,
                    dept, title, employmentType, hire, termination, location, monthlyBase, payFreq, null));
        }
        return list;
    }

    /**
     * Builds 1 to 3 non-overlapping tenures for an employee. The latest tenure
     * matches the employee's current employer and title; earlier ones are
     * plausible prior roles.
     */
    public static List<DemoEmploymentRecord> employmentHistory(DemoEmployee employee) {
        SeededRandom rnd = new SeededRandom(employee.employeeNumber().hashCode() & 0xFFFFL);
        List<DemoEmployer> employers = employers();
        List<DemoEmploymentRecord> history = new ArrayList<>();

        LocalDate currentStart = employee.hireDate();
        String source = "Synthetic employer payroll feed";
        double confidence = 0.97;

        history.add(new DemoEmploymentRecord(employee.employeeNumber(), employee.employerName(),
                employee.jobTitle(), employee.department(), employee.status(), employee.employmentType(),
                currentStart, employee.terminationDate(), employee.workLocation(), source, confidence));

        // ~65% of employees have one to three prior tenures that end before currentStart
        int priorJobs = 0;
        if (rnd.chance(65)) {
            priorJobs = 1;
            if (rnd.chance(50)) {
                priorJobs = 2;
                if (rnd.chance(25)) {
                    priorJobs = 3;
                }
            }
        }
        LocalDate cursor = currentStart.minusDays(1);
        for (int i = 0; i < priorJobs && cursor.isAfter(LocalDate.of(2014, 1, 1)); i++) {
            DemoEmployer prior = pick(rnd, employers);
            if (prior.name().equals(employee.employerName())) {
                continue;
            }
            int tenureDays = rnd.between(300, 1100);
            LocalDate end = cursor.minusDays(rnd.between(5, 40));
            LocalDate start = end.minusDays(tenureDays);
            if (start.isBefore(LocalDate.of(2013, 1, 1))) {
                break;
            }
            history.add(new DemoEmploymentRecord(employee.employeeNumber(), prior.name(),
                    priorTitle(rnd, employee.jobTitle()), pick(rnd, DEPARTMENTS),
                    DemoEmployee.STATUS_TERMINATED, employee.employmentType(),
                    start, end, prior.city() + " - HQ Campus", source, 0.94));
            cursor = start.minusDays(1);
        }
        return history;
    }

    /**
     * Generates pay periods from the later of (hire, lookback start) up to
     * termination or today, aligned to the pay frequency. Gross pay equals
     * base + overtime + bonus + commission; deductions are ~11-16% of gross;
     * net = gross - deductions.
     */
    public static List<DemoPayPeriod> payPeriods(DemoEmployee employee, LocalDate lookbackStart) {
        SeededRandom rnd = new SeededRandom((employee.employeeNumber() + "#pay").hashCode() & 0xFFFFL);
        List<DemoPayPeriod> periods = new ArrayList<>();

        LocalDate employmentStart = employee.hireDate().isAfter(lookbackStart) ? employee.hireDate() : lookbackStart;
        LocalDate end = employee.terminationDate() != null ? employee.terminationDate() : TODAY;
        if (end.isAfter(TODAY)) {
            end = TODAY;
        }
        if (!end.isAfter(employmentStart)) {
            return periods;
        }

        String freq = employee.payFrequency();
        int stepDays = switch (freq) {
            case "WEEKLY" -> 7;
            case "BIWEEKLY" -> 14;
            case "SEMIMONTHLY" -> 0; // special: 1st and 16th
            default -> 30;
        };

        long monthlyBase = employee.monthlyBaseIncomeInr();
        long basePerPeriod = switch (freq) {
            case "WEEKLY" -> Math.round(monthlyBase * 12.0 / 52.0);
            case "BIWEEKLY" -> Math.round(monthlyBase * 12.0 / 26.0);
            case "SEMIMONTHLY" -> monthlyBase / 2;
            default -> monthlyBase;
        };

        LocalDate cursor = employmentStart;
        // keep going while a full period fits before end
        while (cursor.isBefore(end)) {
            LocalDate periodStart;
            LocalDate periodEnd;
            LocalDate payDate;
            if ("SEMIMONTHLY".equals(freq)) {
                int half = cursor.getDayOfMonth() <= 15 ? 0 : 1;
                if (half == 0) {
                    periodStart = cursor.withDayOfMonth(1);
                    periodEnd = cursor.withDayOfMonth(15);
                    payDate = cursor.withDayOfMonth(Math.min(22, cursor.lengthOfMonth()));
                    cursor = cursor.withDayOfMonth(16);
                } else {
                    periodStart = cursor.withDayOfMonth(16);
                    periodEnd = cursor.withDayOfMonth(cursor.lengthOfMonth());
                    payDate = cursor.withDayOfMonth(cursor.lengthOfMonth());
                    cursor = cursor.plusMonths(1).withDayOfMonth(1);
                }
            } else {
                periodStart = cursor;
                periodEnd = cursor.plusDays(stepDays - 1);
                payDate = periodEnd.plusDays(2);
                cursor = cursor.plusDays(stepDays);
            }
            if (payDate.isAfter(end)) {
                break;
            }

            boolean hasBonus = rnd.chance(9);
            boolean hasOvertime = rnd.chance(14);
            boolean hasCommission = rnd.chance(7) && ("Sales".equalsIgnoreCase(employee.department()) || "Marketing".equalsIgnoreCase(employee.department()));

            long overtime = hasOvertime ? Math.round(basePerPeriod * (0.08 + rnd.nextInt(10) / 100.0)) : 0;
            long bonus = hasBonus ? rnd.between(5_000, 45_000) : 0;
            long commission = hasCommission ? rnd.between(8_000, 60_000) : 0;
            double hours = "FULL_TIME".equals(employee.employmentType())
                    ? (freq.equals("WEEKLY") ? 40 : freq.equals("BIWEEKLY") ? 80 : freq.equals("SEMIMONTHLY") ? 86 : 176)
                    : Math.round(rnd.between(40, 90));

            long gross = basePerPeriod + overtime + bonus + commission;
            long deductions = Math.round(gross * (0.11 + rnd.nextInt(6) / 100.0));
            long net = gross - deductions;

            periods.add(new DemoPayPeriod(employee.employeeNumber(), payDate, periodStart, periodEnd,
                    gross, net, basePerPeriod, overtime, bonus, commission, hours, deductions, freq));
        }
        return periods;
    }

    /** Default lookback window: 36 months before today. */
    public static LocalDate defaultLookbackStart() {
        return TODAY.minusMonths(36);
    }

    // ------------------------------------------------------------------
    // Fixed scenario employees (deterministic IDs used by the interview
    // demo launcher and automated tests)
    // ------------------------------------------------------------------

    public static DemoEmployee scenarioPerfect() {
        return new DemoEmployee(SCENARIO_PERFECT, "Rahul Sharma", LocalDate.of(1992, 4, 14),
                "rahul.sharma@acme-demo.com", "+91 98765 43210",
                "Acme Technologies Pvt Ltd", DemoEmployee.STATUS_ACTIVE, "Engineering",
                "Senior Software Engineer", "FULL_TIME", LocalDate.of(2023, 8, 12), null,
                "Bengaluru - HQ Campus", 70_000L, "MONTHLY", "PERFECT");
    }

    public static DemoEmployee scenarioMismatch() {
        // Same person as perfect but the record name differs slightly; used for NO_MATCH demos.
        return new DemoEmployee(SCENARIO_MISMATCH, "R. Sharma", LocalDate.of(1992, 4, 14),
                "r.sharma@acme-demo.com", "+91 98765 43211",
                "Acme Technologies Pvt Ltd", DemoEmployee.STATUS_ACTIVE, "Engineering",
                "Senior Software Engineer", "FULL_TIME", LocalDate.of(2023, 8, 12), null,
                "Bengaluru - HQ Campus", 70_000L, "MONTHLY", "MISMATCH");
    }

    public static DemoEmployee scenarioIncomeAnomaly() {
        // Salary jumped abnormally mid-employment - used for income anomaly + Scenario D.
        return new DemoEmployee(SCENARIO_INCOME, "Priya Nair", LocalDate.of(1990, 11, 2),
                "priya.nair@bluepeak-demo.com", "+91 98860 11223",
                "BluePeak Financial", DemoEmployee.STATUS_ACTIVE, "Finance",
                "Financial Analyst", "FULL_TIME", LocalDate.of(2021, 6, 21), null,
                "Mumbai - Tech Park", 210_000L, "MONTHLY", "INCOME_ANOMALY");
    }

    public static DemoEmployee scenarioDuplicate() {
        // Two records share this employee number across employers - Scenario E duplicate identity.
        return new DemoEmployee(SCENARIO_FRAUD, "Vikram Rao", LocalDate.of(1988, 2, 18),
                "vikram.rao@suspicious-demo.com", "+91 90000 55555",
                "Trident Fintech", DemoEmployee.STATUS_ACTIVE, "Operations",
                "Operations Executive", "FULL_TIME", LocalDate.of(2022, 3, 1), null,
                "Hyderabad - Branch Office", 45_000L, "MONTHLY", "FRAUD");
    }

    public static DemoEmployee scenarioManual() {
        // Active at a low-coverage employer; payroll data intentionally sparse - Scenario H.
        return new DemoEmployee(SCENARIO_MANUAL, "Farhan Ali", LocalDate.of(1994, 7, 9),
                "farhan.ali@kite-demo.com", "+91 90123 45678",
                "Kite Education Group", DemoEmployee.STATUS_ACTIVE, "Operations",
                "Operations Executive", "FULL_TIME", LocalDate.of(2024, 2, 5), null,
                "Jaipur - Branch Office", 38_000L, "MONTHLY", "MANUAL_FALLBACK");
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static long monthlyBase(SeededRandom rnd, String title) {
        return switch (title) {
            case "Engineering Manager" -> rnd.between(180_000, 260_000);
            case "Senior Consultant", "Lead Engineer", "Sales Manager", "Senior Software Engineer" -> rnd.between(120_000, 190_000);
            case "Software Engineer", "Financial Analyst", "Data Analyst", "QA Engineer" -> rnd.between(60_000, 120_000);
            case "Product Analyst", "Marketing Specialist", "HR Business Partner" -> rnd.between(50_000, 95_000);
            default -> rnd.between(32_000, 70_000);
        };
    }

    private static String priorTitle(SeededRandom rnd, String currentTitle) {
        List<String> options = List.of("Software Engineer", "Associate Consultant", "QA Engineer", "Data Analyst", "Operations Executive");
        String t = pick(rnd, options);
        return t.equals(currentTitle) ? "Software Engineer" : t;
    }

    public static String emailDomain(String employerName) {
        String slug = employerName.toLowerCase(Locale.ROOT)
                .replaceAll("pvt ltd|private limited|group|services|labs|india", "").trim();
        slug = slug.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return slug + ".demo";
    }

    private static <T> T pick(SeededRandom rnd, List<T> items) {
        return items.get(rnd.nextInt(items.size()));
    }
}
