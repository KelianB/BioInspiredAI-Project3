package utils;

import java.util.ArrayList;
import java.util.List;

public class GanttChart {
	private List<GanttTask>[] rows;
	
	@SuppressWarnings("unchecked")
	public GanttChart(int rows) {
		this.rows = new ArrayList[rows];
		for(int i = 0; i < rows; i++)
			this.rows[i] = new ArrayList<GanttTask>();
	}
	
	public void addTask(int row, int category, int time, int duration) {
		this.rows[row].add(new GanttTask(category, time, duration));
		this.rows[row].sort((a,b) -> a.time - b.time);
	}
	
	public boolean test() {
		for(int i = 0; i < rows.length; i++) {
			List<GanttTask> row = rows[i];
					
			for(int j = 0; j < row.size() - 1; j++)
				if(row.get(j).time + row.get(j).duration > row.get(j+1).time)
					return false;
		}
		return true;
	}
	
	/*@Override
	public String toString() {
		String str = "";
		for(int i = 0; i < rows.length; i++) {
			List<GanttTask> row = rows[i];
			
			String rowstr = "";
			for(int j = 0; j < row.size(); j++) {
				String taskStr = " " + row.get(j).time + "," + row.get(j).duration;
				while(taskStr.length() < 6)
					taskStr += " ";
				rowstr += taskStr;
			}
			str += rowstr + (i < rows.length-1 ? "\n" : "");
		}
		return str;
	}*/
	
	@Override
	public String toString() {
		String str = "";
		for(int i = 0; i < rows.length; i++) {
			List<GanttTask> row = rows[i];
			
			String rowstr = "";
			for(int j = 0; j < row.size(); j++) {
				while(row.get(j).time - rowstr.length() > 0)
					rowstr += " ";
				rowstr += "[";
				for(int k = 0; k < row.get(j).duration-2; k++)
					rowstr += "|";
				rowstr += "]";
			}
			str += rowstr + (i < rows.length-1 ? "\n" : "");
		}
		return str;
	}
	
	
	
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
