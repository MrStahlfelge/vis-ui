/*
 * Copyright 2014-2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotcrab.vis.editor.proxy;

import com.artemis.Entity;
import com.badlogic.gdx.math.Rectangle;
import com.kotcrab.vis.editor.module.scene.system.render.PointRenderSystem;
import com.kotcrab.vis.runtime.component.PointComponent;
import com.kotcrab.vis.runtime.properties.BoundsOwner;
import com.kotcrab.vis.runtime.properties.SizeOwner;

/** @author Kotcrab */
public class PointProxy extends EntityProxy {
	private PointComponent pos;
	private Accessor accessor;
	private float renderSize;

	public PointProxy (Entity entity, float pixelsPerUnit) {
		super(entity);
		renderSize = PointRenderSystem.ICON_SIZE / pixelsPerUnit;
	}

	@Override
	protected void createAccessors () {
		accessor = new Accessor();
	}

	@Override
	protected void reloadAccessors () {
		pos = getEntity().getComponent(PointComponent.class);
		enableBasicProperties(pos, accessor, accessor);
	}

	@Override
	public String getEntityName () {
		return "Point";
	}

	private class Accessor implements SizeOwner, BoundsOwner {
		private Rectangle bounds = new Rectangle();

		public Accessor () {
			bounds = new Rectangle();
		}

		@Override
		public float getWidth () {
			return renderSize;
		}

		@Override
		public float getHeight () {
			return renderSize;
		}

		@Override
		public Rectangle getBoundingRectangle () {
			return bounds.set(pos.x - renderSize / 2, pos.y - renderSize / 2, renderSize, renderSize);
		}
	}
}
