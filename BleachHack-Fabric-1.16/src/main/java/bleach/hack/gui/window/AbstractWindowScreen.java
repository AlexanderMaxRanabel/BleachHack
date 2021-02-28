/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.gui.window;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class AbstractWindowScreen extends Screen {

	public List<Window> windows = new ArrayList<>();
	public int overlapColor;
	public boolean useOverlap = false;

	public AbstractWindowScreen(Text text_1) {
		super(text_1);
	}
	
	public AbstractWindowScreen(Text text_1, int overlapColor) {
		super(text_1);
		this.overlapColor = overlapColor;
		useOverlap = true;
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		boolean close = true;
		int noneSelected = -1;
		int selected = -1;
		int count = 0;
		for (Window w : windows) {
			if (!w.closed) {
				close = false;
				if (!w.selected) {
					onRenderWindow(matrix, count, mouseX, mouseY);
				} else {
					selected = count;
				}

				if (noneSelected >= -1)
					noneSelected = count;
			}

			if (w.selected && !w.closed) {
				noneSelected = -2;
			}
			count++;
		}

		if (selected >= 0)
			onRenderWindow(matrix, selected, mouseX, mouseY);
		if (noneSelected >= 0)
			windows.get(noneSelected).selected = true;
		if (close)
			this.onClose();

		super.render(matrix, mouseX, mouseY, delta);
	}

	public void onRenderWindow(MatrixStack matrix, int window, int mX, int mY) {
		if (useOverlap && windows.get(window).selected) {
			Window sel = windows.get(window);
			for (Window cur: windows) {
				if (!cur.selected && !cur.closed) {
					if (cur.x1 < sel.x2 && cur.y1 < sel.y2 && cur.x2 > sel.x1 && cur.y2 > sel.y1) {
						DrawableHelper.fill(matrix,
								Math.max(cur.x1, sel.x1) + 1,
								Math.max(cur.y1, sel.y1) + 1,
								Math.min(cur.x2, sel.x2) - 1,
								Math.min(cur.y2, sel.y2) - 1,
								overlapColor);
					}
				}
			}
		}
		
		if (!windows.get(window).closed) {
			windows.get(window).render(matrix, mX, mY);
		}
	}

	public void selectWindow(int window) {
		int count = 0;
		for (Window w : windows) {
			if (w.selected) {
				w.inactiveTime = 2;
			}

			w.selected = (count == window);
			count++;
		}
	}

	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		/* Handle what window will be selected when clicking */
		int count = 0;
		int nextSelected = -1;
		for (Window w : windows) {
			if (w.selected) {
				w.onMousePressed((int) double_1, (int) double_2);
			}

			if (w.shouldClose((int) double_1, (int) double_2))
				w.closed = true;

			if (w.inactiveTime <= 0 && double_1 > w.x1 && double_1 < w.x2 && double_2 > w.y1 && double_2 < w.y2 && !w.closed) {
				if (w.selected) {
					nextSelected = -1;
					break;
				} else {
					nextSelected = count;
				}
			}
			count++;
		}

		if (nextSelected >= 0) {
			for (Window w : windows)
				w.selected = false;
			windows.get(nextSelected).selected = true;
		}

		return super.mouseClicked(double_1, double_2, int_1);
	}

	public boolean mouseReleased(double double_1, double double_2, int int_1) {
		for (Window w : windows) {
			w.onMouseReleased((int) double_1, (int) double_2);
		}

		return super.mouseReleased(double_1, double_2, int_1);
	}

}
