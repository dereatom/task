package services;

import dao.TaskDaoImpl;
import dao.UserDaoImpl;
import domain.Task;
import domain.TaskStatus;
import exceptions.ForbiddenOperationException;
import util.UserInput;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class TaskService {
    TaskDaoImpl taskDao = new TaskDaoImpl();
    UserDaoImpl userDao = new UserDaoImpl();

    public void showTasks(int userId) {
        List<Task> tasks = taskDao.readAll(userId);
        this.printTasks(tasks);
    }

    public void addTask(int userId) {
        System.out.println("Please provide the title for your new task:");
        String title = UserInput.getStringInput();
        System.out.println("Please provide a description for your task:");
        String description = UserInput.getStringInput();
        System.out.println("What is the deadline for the task? Write in this format: MM/DD/YYYY");
        String[] dateArr = UserInput.getStringInput().split("/");
        LocalDateTime dueDate = LocalDate.of(Integer.valueOf(dateArr[2]), Integer.valueOf(dateArr[0]), Integer.valueOf(dateArr[1])).atStartOfDay();
        Task task = new Task(dueDate, LocalDateTime.now(), title, description, TaskStatus.NOT_STARTED, userId);

        taskDao.create(task);
        System.out.println("You have successfully created a task!");
        showTasks(userId);
    }

    public void updateTask(int userId) {
        Task task = chooseTask(userId);
        System.out.println("Choose a new title or leave blank (just press enter) if you want to leave it unchanged:");

        String newTitle = UserInput.getStringInput();
        if (!newTitle.isBlank() && !newTitle.isEmpty()) {
            task.setTitle(newTitle);
        }
        System.out.println("Add a new description or leave blank (just press enter) if you want to leave it unchanged:");
        String newDescription = UserInput.getStringInput();
        if (!newDescription.isEmpty() && !newDescription.isBlank()) {
            task.setDescription(newDescription);
        }

        System.out.println("What will the new status of your task be? (leave blank -- just press enter -- to leave it unchanged)\n" +
                "(1) NOT STARTED" +
                "(2) IN PROGRESS" +
                "(3) COMPLETED");
        Integer userInput = UserInput.getIntInput();
        UserInput.getStringInput();
        TaskStatus newStatus;

        if (userInput != null) {
            switch (userInput) {
                case 1:
                    newStatus = TaskStatus.NOT_STARTED;
                    break;
                case 2:
                    newStatus = TaskStatus.IN_PROGRESS;
                    break;
                case 3:
                    newStatus = TaskStatus.FINISHED;
                    break;
                default:
                    newStatus = task.getStatus();
                    break;
            }

            task.setStatus(newStatus);
        }
        System.out.println("What date should your task be completed by? Please input in this format: MM/DD/YYYY (leave blank -- just press enter -- if you want to leave unchanged");
        String dateStr = UserInput.getStringInput();
        if (!dateStr.isBlank() && !dateStr.isEmpty()) {
            String[] dateArr = dateStr.split("/");
            int year = Integer.valueOf(dateArr[2]);
            int day = Integer.valueOf(dateArr[1]);
            int month = Integer.valueOf(dateArr[0]);
            LocalDateTime dueDate = LocalDate.of(year, month, day).atStartOfDay();


            task.setDueDate(dueDate);
        }
        taskDao.update(task);
        System.out.println("Task was successfully updated!");

    }

    private Task chooseTask(int userId) {
        List<Task> myTasks = taskDao.readAll(userId);
        System.out.println("Select the task id of the task you want to update:");
        for (Task t : myTasks) {
            System.out.println(t);
        }

        int taskId = UserInput.getIntInput();
        UserInput.getStringInput();
        checkTaskBelongsToUser(userId, taskId);
        Task task = taskDao.read(taskId);
        return task;
    }

    public Task updateStatus(int userId) {
        Task task = chooseTask(userId);
        while (true) {
            System.out.println("Choose new status:\n" +
                    "(1) NOT STARTED\n" +
                    "(2) IN PROGRESS\n" +
                    "(3) COMPLETED\n" +
                    "(4) Exit operation\n" +
                    "(5) Exit program");
            int input = UserInput.getIntInput();
            UserInput.getStringInput();
            switch (input) {
                case 1:
                    task.setStatus(TaskStatus.NOT_STARTED);
                    taskDao.update(task);
                    return task;
                case 2:
                    task.setStatus(TaskStatus.IN_PROGRESS);
                    taskDao.update(task);
                    return task;
                case 3:
                    task.setStatus(TaskStatus.FINISHED);
                    taskDao.update(task);
                    return task;
                case 4:
                    return null;
                case 5:
                    System.out.println("Thank you for playing!\n");
                    System.exit(0);
                default:
                    System.out.println("Invalid input, please try again\n");
                    break;
            }
        }
    }


    public List<Task> sortTasksByStartDate(int userId) {
        List<Task> tasks = taskDao.readAll(userId);
        Collections.sort(tasks, new TaskComparatorStartDate());
        return tasks;
    }

    public List<Task> sortTasksByDueDate(int userId) {
        List<Task> tasks = taskDao.readAll(userId);
        Collections.sort(tasks, new TaskComparatorDueDate());
        return tasks;
    }

    public List<Task> getUnstartedTasks(int userId) {
        return taskDao.readAll(userId).stream().filter(task -> task.getStatus().equals(TaskStatus.NOT_STARTED)).toList();

    }

    public void showUnstartedTasks(int userId) {
        List<Task> unstartedTasks = getUnstartedTasks(userId);
        this.printTasks(unstartedTasks);

    }

    public void printTasks(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {

            Task task = tasks.get(i);
            LocalDateTime startDate = task.getStartDate();
            LocalDateTime dueDate = task.getDueDate();
            System.out.println(i + 1 + ") \n" +
                    "TASK ID: " + task.getTaskId() + "\n\tTASK TITLE: " + task.getTitle());
            System.out.println("\t\tTASK STATUS: " + task.getStatus());
            System.out.println("\t\tTASK DESCRIPTION: " + task.getDescription());
            System.out.println("\t\tTASK STARTED ON: " + startDate.getMonth() + " " + startDate.getDayOfMonth() + ", " + startDate.getYear() + "\n\t\tTASK DUE ON: " + dueDate.getMonth() + " " + dueDate.getDayOfMonth() + ", " + dueDate.getYear());

            System.out.println("\n\n");
        }
    }

    public List<Task> getCompletedTasks(int userId) {
        return taskDao.readAll(userId).stream().filter(task -> task.getStatus().equals(TaskStatus.FINISHED)).toList();

    }

    public void showCompletedTasks(int userId) {
        List<Task> completedTasks = getCompletedTasks(userId);
        printTasks(completedTasks);
    }

    List<Task> getInProgressTasks(int userId) {
        return taskDao.readAll(userId).stream().filter(task -> task.getStatus().equals(TaskStatus.IN_PROGRESS)).toList();
    }

    public void showInProgressTasks(int userId) {
        List<Task> inProgressTasks = getInProgressTasks(userId);
        printTasks(inProgressTasks);
    }

    public void completeTask(int userId) {
        System.out.println("Please choose the task you'd like to mark as completed by ID:");

        List<Task> tasks = taskDao.readAll(userId);
        printTasks(tasks);

        int taskId = UserInput.getIntInput();
        UserInput.getStringInput();
        checkTaskBelongsToUser(userId, taskId);
        Task task = taskDao.read(taskId); //handle exception at some point

        task.setStatus(TaskStatus.FINISHED);
        taskDao.update(task);
        System.out.println("Your task has been successfully completed!");

    }

    private void checkTaskBelongsToUser(int userId, int taskId) {
        List<Task> tasks = taskDao.readAll(userId);
        if (tasks.stream().noneMatch(task -> task.getTaskId() == taskId)) {
            if (taskDao.taskExists(taskId)) {
                throw new ForbiddenOperationException("Task exists but it does not belong to logged-in user.\n");
            } else {
                throw new ForbiddenOperationException("There is no task on record with the provided id");
            }

        }
    }

    public void deleteTask(int userId) {
        List<Task> tasks = taskDao.readAll(userId);
        System.out.println("Please input the task ID of the task you'd like to remove:\n");
        printTasks(tasks);
        int input = UserInput.getIntInput();
        UserInput.getStringInput();
        checkTaskBelongsToUser(userId, input);
        taskDao.delete(input);
        System.out.println("You have successfully deleted the task from your list!");

    }
}
