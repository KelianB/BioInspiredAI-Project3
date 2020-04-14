package utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Gantt-Chart representation.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class GanttChart {
	// Rows of the chart, as lists of tasks
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
	 * @param indexInCategory - The index of the task within its category (for display purposes only)
	 * @param time - The time at which this task starts being executed
	 * @param duration - The task duration
	 */
	public void addTask(int row, int category, int indexInCategory, int time, int duration) {
		// Add the new task to the corresponding row and keep the row sorted by time
		this.rows[row].add(new GanttTask(category, indexInCategory, time, duration));
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
				GanttTask lastTask = this.rows[i].get(this.rows[i].size() - 1);
				int rowEndTime = lastTask.time + lastTask.duration;
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
		private int indexInCategory;
		private int time;
		private int duration;
		
		public GanttTask(int category, int indexInCategory, int time, int duration) {
			this.category = category;
			this.time = time;
			this.duration = duration;
			this.indexInCategory=indexInCategory;
		}
	}

	/***
	 * Generate an image from this Gantt-Chart.
	 * @return an image representing this chart
	 */
	public BufferedImage generateImage() {		
		int w = 1200, h = 900;
		
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// Background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);

		g.setFont(new Font("SansSerif", Font.PLAIN, 20));
		g.setColor(Color.BLACK);
		
		int leftX = 140;
		
		w = getEndTime() + 500;
		float timeScale = (w-280) / getEndTime();
		float machineScale = (h-40) / rows.length;
		
		// time scale
		// draw the time line
		g.setColor(Color.BLACK);
		for(int i = leftX; i < w-80; i++) {
			float axisPos = (i-leftX) / timeScale;
			if(axisPos % (getEndTime()/10) == 0) {
				String xLabel = (int) axisPos + "";
				g.setColor(Color.BLACK);
				g.drawString(xLabel, i - g.getFontMetrics().stringWidth(xLabel) / 2, h-10);
				g.drawLine(i, 0, i, h-35);
			}
			else if(axisPos  % (int) (getEndTime()/50) == 0) {
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(i, 0, i, h-45);
			}
		}

		// generate list of colors
		int njobs = rows[0].size();
		List<Color> colors = new ArrayList<Color>();
		Random rand = new Random();
		for(int i = 0; i < njobs; i++) {
			float min = 0.2f, max = 1.0f;
			Color randomColor = new Color(min + rand.nextFloat() * (max-min), min + rand.nextFloat() * (max-min), min + rand.nextFloat() * (max-min));
			colors.add(randomColor);
		}

		// fill the schedule
		for(int machine = 0; machine < rows.length; machine++) {
			for(GanttTask operation : rows[machine]) {
				int operationW = (int) (operation.duration * timeScale);
				
				g.setColor(colors.get(operation.category));
				g.fillRect((int) (leftX + operation.time*timeScale), (int) (machine*machineScale), operationW, (int) (machineScale));
				
				int x = (int) (leftX + operation.time * timeScale);
				int y = (int) (machine*machineScale + machineScale / 2);
				
				g.setColor(Color.BLACK);
				g.setFont(new Font("SansSerif", Font.BOLD, 18));
				String label = (operation.category + 1) + "";
				g.drawString(label, x + (operationW - g.getFontMetrics().stringWidth(label)) / 2, y-5);
				
				g.setFont(new Font("SansSerif", Font.PLAIN, 15));
				label = (operation.indexInCategory + 1) + "";
				g.drawString(label, x + (operationW - g.getFontMetrics().stringWidth(label)) / 2, y+15);
			}
		}
		
		// machines scale
		g.setColor(Color.BLACK);
		g.setFont(new Font("SansSerif", Font.PLAIN, 20));
		for(int i = 0; i < rows.length; i++) {
			String yLabel = "Machine " + (i+1);
			g.drawLine(leftX, (int) (i*machineScale), w, (int) (i*machineScale));
			g.drawString(yLabel, 10, i*machineScale + machineScale/2 + 10);
		}
		g.drawLine(leftX, h-45, w, h-45);

		g.dispose();
		return img;
	}
}
