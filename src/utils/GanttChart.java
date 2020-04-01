package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Gantt-Chart representation.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class GanttChart {
	private List<GanttTask>[] rows;
	
	/**
	 * Create a Gantt-Chart with the given number of rows.
	 * @param rows - The number of rows in the chart
	 */
	@SuppressWarnings("unchecked")
	public GanttChart(int rows) {
		this.rows = new ArrayList[rows];
		for(int i = 0; i < rows; i++)
			this.rows[i] = new ArrayList<GanttTask>();
	}
	
	/**
	 * Add a task to the Gantt-Chart
	 * @param row - A row index in the chart
	 * @param category - The category (color) of the task (note: two tasks of the same category cannot be executed in parallel)
	 * @param time - The time at which this task starts being executed
	 * @param duration - The task duration
	 */
	public void addTask(int row, int category, int time, int duration) {
		this.rows[row].add(new GanttTask(category, time, duration));
		this.rows[row].sort((a,b) -> a.time - b.time);
	}
	
	/**
	 * Get the time at which all tasks are finished (i.e. the makespan).
	 * @return the end time
	 */
	public int getEndTime() {
		int endTime = 0;
		for(int i = 0; i < this.rows.length; i++) {
			if(this.rows[i].size() >= 1) {
				int rowEndTime = this.rows[i].get(this.rows[i].size() - 1).time;
				if(rowEndTime > endTime)
					endTime = rowEndTime;
			}
		}
		return endTime;
	}
	
	/**
	 * Test the validity of the chart.
	 * @return true if this is a valid Gantt Chart, else false
	 */
	public boolean test() {
		HashMap<Integer, List<GanttTask>> byCategory = new HashMap<Integer, List<GanttTask>>();

		// Check that two tasks don't overlap in one row (and fill map of tasks by category)
		for(int i = 0; i < rows.length; i++) {
			List<GanttTask> row = rows[i];
					
			for(int j = 0; j < row.size() - 1; j++) {
				GanttTask t = row.get(j);
				
				if(t.time + t.duration > row.get(j+1).time)
					return false;
				
				if(!byCategory.containsKey(t.category))
					byCategory.put(t.category, new ArrayList<GanttTask>());
				byCategory.get(t.category).add(t);
			}
		}
		
		// Check that two tasks of the same category don't overlap
		for(int category : byCategory.keySet()) {
			List<GanttTask> tasks = byCategory.get(category);
			tasks.sort((a,b) -> a.time - b.time);
			
			for(int j = 0; j < tasks.size() - 1; j++) {
				GanttTask t = tasks.get(j);
				if(t.time + t.duration > tasks.get(j+1).time)
					return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		String str = "";
		for(int i = 0; i < rows.length; i++) {
			List<GanttTask> row = rows[i];
			
			String rowstr = "";
			for(int j = 0; j < row.size(); j++) {
				while(rowstr.length() < row.get(j).time)
					rowstr += " ";
				for(int k = 0; k < row.get(j).duration; k++)
					rowstr += row.get(j).category;
			}
			str += rowstr + (i < rows.length-1 ? "\n" : "");
		}
		return str;
	}
	
	/***
	 * Store a task in a Gantt-Chart
	 */
	static class GanttTask {
		private int category;
		private int time;
		private int duration;
		
		public GanttTask(int category, int time, int duration) {
			this.category = category;
			this.time = time;
			this.duration = duration;
		}
	}
}
