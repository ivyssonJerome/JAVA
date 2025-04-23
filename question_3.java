import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class TimeInterval {
    private LocalDateTime start;
    private LocalDateTime end;
    private TrainSchedule schedule;

    public TimeInterval(LocalDateTime start, LocalDateTime end, TrainSchedule schedule) {
        this.start = start;
        this.end = end;
        this.schedule = schedule;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public TrainSchedule getSchedule() {
        return schedule;
    }
}

class ScheduleEntry {
    private Station station;
    private int platform;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;

    public ScheduleEntry(Station station, int platform, LocalDateTime arrivalTime, LocalDateTime departureTime) {
        this.station = station;
        this.platform = platform;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    public Station getStation() {
        return station;
    }

    public int getPlatform() {
        return platform;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }
}

class TrainSchedule {
    private String trainId;
    private List<ScheduleEntry> entries;

    public TrainSchedule(String trainId, List<ScheduleEntry> entries) {
        this.trainId = trainId;
        this.entries = entries;
    }

    public String getTrainId() {
        return trainId;
    }

    public List<ScheduleEntry> getEntries() {
        return entries;
    }
}

class Station {
    private String name;
    private List<Platform> platforms;

    public Station(String name, int numberOfPlatforms) {
        this.name = name;
        this.platforms = new ArrayList<>();
        for (int i = 1; i <= numberOfPlatforms; i++) {
            platforms.add(new Platform(i));
        }
    }

    public String getName() {
        return name;
    }

    public Platform getPlatform(int number) {
        for (Platform platform : platforms) {
            if (platform.getNumber() == number) {
                return platform;
            }
        }
        return null;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }
}

class Platform {
    private int number;
    private List<TimeInterval> reservations = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();

    public Platform(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public List<TimeInterval> getReservations() {
        return reservations;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}

class Train extends Thread {
    private TrainSchedule schedule;
    private String trainId;

    public Train(TrainSchedule schedule) {
        this.schedule = schedule;
        this.trainId = schedule.getTrainId();
    }

    @Override
    public void run() {
        try {
            for (ScheduleEntry entry : schedule.getEntries()) {
                LocalDateTime arrivalTime = entry.getArrivalTime();
                LocalDateTime departureTime = entry.getDepartureTime();

                long delay = calculateDelay(arrivalTime);
                if (delay > 0) {
                    Thread.sleep(delay);
                }
                System.out.println("Train " + trainId + " arrived at " + entry.getStation().getName() + " Platform " + entry.getPlatform() + " at " + arrivalTime);

                delay = calculateDelay(departureTime);
                if (delay > 0) {
                    Thread.sleep(delay);
                }
                System.out.println("Train " + trainId + " departed from " + entry.getStation().getName() + " Platform " + entry.getPlatform() + " at " + departureTime);
            }
        } catch (InterruptedException e) {
            System.out.println("Train " + trainId + " was canceled.");
        }
    }

    private long calculateDelay(LocalDateTime targetTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, targetTime);
        return duration.toMillis();
    }
}

public class TrainScheduler {
    private List<Station> stations = new ArrayList<>();
    private List<TrainSchedule> trainSchedules = new ArrayList<>();
    private Map<TrainSchedule, Train> activeTrains = new HashMap<>();

    public void addStation(Station station) {
        stations.add(station);
    }

    public boolean addTrainSchedule(TrainSchedule schedule) {
        List<Platform> platformsToReserve = new ArrayList<>();
        for (ScheduleEntry entry : schedule.getEntries()) {
            Station station = entry.getStation();
            Platform platform = station.getPlatform(entry.getPlatform());
            if (platform == null) {
                return false;
            }
            platformsToReserve.add(platform);
        }

        platformsToReserve.sort(Comparator.comparing(p -> p.getStation().getName() + p.getNumber()));

        try {
            for (Platform p : platformsToReserve) {
                p.lock();
            }

            for (int i = 0; i < schedule.getEntries().size(); i++) {
                ScheduleEntry entry = schedule.getEntries().get(i);
                Platform platform = platformsToReserve.get(i);
                if (!isPlatformAvailable(platform, entry.getArrivalTime(), entry.getDepartureTime())) {
                    return false;
                }
            }

            for (int i = 0; i < schedule.getEntries().size(); i++) {
                ScheduleEntry entry = schedule.getEntries().get(i);
                Platform platform = platformsToReserve.get(i);
                platform.getReservations().add(new TimeInterval(entry.getArrivalTime(), entry.getDepartureTime(), schedule));
            }

            synchronized (trainSchedules) {
                trainSchedules.add(schedule);
            }
            Train train = new Train(schedule);
            activeTrains.put(schedule, train);
            train.start();
            return true;
        } finally {
            for (int i = platformsToReserve.size() - 1; i >= 0; i--) {
                platformsToReserve.get(i).unlock();
            }
        }
    }

    private boolean isPlatformAvailable(Platform platform, LocalDateTime start, LocalDateTime end) {
        for (TimeInterval interval : platform.getReservations()) {
            if (interval.getStart().isBefore(end) && interval.getEnd().isAfter(start)) {
                return false;
            }
        }
        return true;
    }

    public boolean cancelTrainSchedule(TrainSchedule schedule) {
        synchronized (trainSchedules) {
            if (!trainSchedules.remove(schedule)) {
                return false;
            }
        }

        Train train = activeTrains.get(schedule);
        if (train != null) {
            train.interrupt();
            activeTrains.remove(schedule);
        }

        List<Platform> platformsToUpdate = new ArrayList<>();
        for (ScheduleEntry entry : schedule.getEntries()) {
            Platform platform = entry.getStation().getPlatform(entry.getPlatform());
            platformsToUpdate.add(platform);
        }

        platformsToUpdate.sort(Comparator.comparing(p -> p.getStation().getName() + p.getNumber()));

        try {
            for (Platform p : platformsToUpdate) {
                p.lock();
            }

            for (ScheduleEntry entry : schedule.getEntries()) {
                Platform platform = entry.getStation().getPlatform(entry.getPlatform());
                Iterator<TimeInterval> it = platform.getReservations().iterator();
                while (it.hasNext()) {
                    TimeInterval interval = it.next();
                    if (interval.getSchedule() == schedule) {
                        it.remove();
                    }
                }
            }
        } finally {
            for (int i = platformsToUpdate.size() - 1; i >= 0; i--) {
                platformsToUpdate.get(i).unlock();
            }
        }
        return true;
    }

    public List<TrainSchedule> getTrainSchedules() {
        return new ArrayList<>(trainSchedules);
    }

    public static void main(String[] args) {
        TrainScheduler scheduler = new TrainScheduler();
        Station stationA = new Station("Station A", 2);
        scheduler.addStation(stationA);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = now.plusMinutes(1);
        LocalDateTime arrivalTime = now.plusMinutes(2);

        ScheduleEntry entry = new ScheduleEntry(stationA, 1, arrivalTime, departureTime);
        List<ScheduleEntry> entries = new ArrayList<>();
        entries.add(entry);

        TrainSchedule schedule = new TrainSchedule("T123", entries);
        boolean added = scheduler.addTrainSchedule(schedule);
        System.out.println("Schedule added: " + added);

        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
