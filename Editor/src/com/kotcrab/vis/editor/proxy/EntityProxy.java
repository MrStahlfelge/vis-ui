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

import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.kotcrab.vis.editor.entity.EntityScheme;
import com.kotcrab.vis.editor.entity.UUIDComponent;
import com.kotcrab.vis.editor.module.scene.entitymanipulator.tool.PolygonTool;
import com.kotcrab.vis.editor.module.scene.system.VisUUIDManager;
import com.kotcrab.vis.editor.util.polygon.Clipper;
import com.kotcrab.vis.runtime.component.*;
import com.kotcrab.vis.runtime.properties.*;

import java.util.UUID;

/** @author Kotcrab */
public abstract class EntityProxy {
	private ComponentMapper<PolygonComponent> polygonCm;

	private Entity entity;
	private VisUUIDManager uuidManager;
	private UUID uuid;

	private PositionOwner positionOwner;
	private SizeOwner sizeOwner;
	private BoundsOwner boundsOwner;

	private TintOwner tintOwner;
	private FlipOwner flipOwner;
	private OriginOwner originOwner;
	private RotationOwner rotationOwner;
	private ScaleOwner scaleOwner;

	private Resizeable resizeable;

	public EntityProxy (Entity entity) {
		this.entity = entity;
		init();

		if (entity != null) {
			uuidManager = entity.getWorld().getSystem(VisUUIDManager.class);
			uuid = entity.getComponent(UUIDComponent.class).getUUID();

			//TODO: [misc] proxies may use injected component mappers to acuire other components, not they are using getComponent on entity directly
			polygonCm = entity.getWorld().getMapper(PolygonComponent.class);
		}
	}

	protected void init () {
		if (entity.getComponent(RenderableComponent.class) == null || entity.getComponent(LayerComponent.class) == null)
			throw new IllegalArgumentException("Proxy cannot be used for non renderable entities. Entity must contain RenderableComponent and LayerComponent");

		createAccessors();
		reloadAccessors();
		checkAccessors();
	}

	/**
	 * Reloads this proxy, must be called if there is chance that this entity was removed and then radded by UndoableAction
	 * or it's components used by proxy changed.
	 */
	public void reload () {
		entity = uuidManager.get(uuid);
		reloadAccessors();
		checkAccessors();
	}

	private void checkAccessors () {
		if (positionOwner == null || sizeOwner == null || boundsOwner == null)
			throw new IllegalStateException("Basic accessors are not set, did you call #enableBasicProperties in #createAccessors?");
	}

	protected abstract void createAccessors ();

	protected abstract void reloadAccessors ();

	public EntityScheme getScheme () {
		return new EntityScheme(entity);
	}

	public void addGroup (int groupId) {
		addGroup(groupId, -1);
	}

	public void addGroup (int groupId, int parentGroupId) {
		IntArray groupIds = getGroupComponent().groupIds;
		if (groupIds.contains(groupId) == false) {
			if (parentGroupId != -1)
				groupIds.insert(groupIds.indexOf(parentGroupId), groupId);
			else
				groupIds.add(groupId);
		}
	}

	public void removeGroup (int groupId) {
		IntArray groupIds = getGroupComponent().groupIds;
		if (groupIds.contains(groupId)) groupIds.removeValue(groupId);
	}

	public int getLastGroupId () {
		GroupComponent gdc = entity.getComponent(GroupComponent.class);

		if (gdc == null || gdc.groupIds.size == 0) {
			return -1;
		}

		return gdc.groupIds.peek();
	}

	public int getGroupIdBefore (int gid) {
		IntArray groupIds = getGroupComponent().groupIds;
		int index = groupIds.indexOf(gid) - 1;

		if (index < 0)
			return -1;
		else
			return groupIds.get(index);
	}

	public int getGroupIdAfter (int gid) {
		IntArray groupIds = getGroupComponent().groupIds;
		int index = groupIds.indexOf(gid) + 1;

		if (index >= groupIds.size)
			return -1;
		else
			return groupIds.get(index);
	}

	public boolean groupsContains (int gid) {
		return getGroupComponent().groupIds.contains(gid);
	}

	public IntArray getGroupsIds () {
		return new IntArray(getGroupComponent().groupIds);
	}

	private GroupComponent getGroupComponent () {
		GroupComponent gdc = entity.getComponent(GroupComponent.class);

		if (gdc == null) {
			gdc = new GroupComponent();
			entity.edit().add(gdc);
		}

		return gdc;
	}

	public String getId () {
		IDComponent idc = entity.getComponent(IDComponent.class);
		if (idc != null)
			return idc.id;
		else
			return null;
	}

	public void setId (String id) {
		IDComponent idc = entity.getComponent(IDComponent.class);

		if (idc != null)
			idc.id = id;
		else
			entity.edit().add(new IDComponent(id));
	}

	public boolean hasComponent (Class<? extends Component> clazz) {
		return entity.getComponent(clazz) != null;
	}

	public int getZIndex () {
		return entity.getComponent(RenderableComponent.class).zIndex;
	}

	public void setZIndex (int zIndex) {
		entity.getComponent(RenderableComponent.class).zIndex = zIndex;
	}

	public int getLayerID () {
		return entity.getComponent(LayerComponent.class).layerId;
	}

	public void setLayerId (int layerId) {
		entity.getComponent(LayerComponent.class).layerId = layerId;
	}

	protected void enableBasicProperties (PositionOwner posOwner, SizeOwner sizeOwner, BoundsOwner boundsOwner) {
		this.positionOwner = posOwner;
		this.sizeOwner = sizeOwner;
		this.boundsOwner = boundsOwner;
	}

	//basic properties

	public float getX () {
		return positionOwner.getX();
	}

	public void setX (float x) {
		updatePolygon(x, getY());
		positionOwner.setX(x);
	}

	public float getY () {
		return positionOwner.getY();
	}

	public void setY (float y) {
		updatePolygon(getX(), y);
		positionOwner.setY(y);
	}

	public void setPosition (float x, float y) {
		updatePolygon(x, y);
		positionOwner.setPosition(x, y);
	}

	protected void updatePolygon (float x, float y) {
		PolygonComponent polygon = polygonCm.getSafe(entity);
		float dx = getX() - x;
		float dy = getY() - y;
		if (polygon != null) {
			for (Vector2 vertex : polygon.vertices) {
				vertex.sub(dx, dy);
			}

			polygon.faces = Clipper.polygonize(PolygonTool.DEFAULT_POLYGONIZER, polygon.vertices.toArray(Vector2.class));
		}
	}

	public float getWidth () {
		return sizeOwner.getWidth();
	}

	public float getHeight () {
		return sizeOwner.getHeight();
	}

	public Rectangle getBoundingRectangle () {
		return boundsOwner.getBoundingRectangle();
	}

	//resize properties

	protected void enableResize (Resizeable resizeable) {
		this.resizeable = this.resizeable;
	}

	public boolean isResizeSupported () {
		return resizeable != null;
	}

	public void setSize (float width, float height) {
		if (resizeable == null) return;
		resizeable.setSize(width, height);
	}

	//origin properties

	protected void enableOrigin (OriginOwner originOwner) {
		this.originOwner = originOwner;
	}

	public boolean isOriginSupported () {
		return originOwner != null;
	}

	public float getOriginX () {
		if (originOwner == null) return 0;
		return originOwner.getOriginX();
	}

	public float getOriginY () {
		if (originOwner == null) return 0;
		return originOwner.getOriginY();
	}

	public void setOrigin (float x, float y) {
		if (originOwner == null) return;
		originOwner.setOrigin(x, y);
	}

	//scale properties

	protected void enableScale (ScaleOwner scaleOwner) {
		this.scaleOwner = scaleOwner;
	}

	public boolean isScaleSupported () {
		return scaleOwner != null;
	}

	public float getScaleX () {
		if (scaleOwner == null) return 0;
		return scaleOwner.getScaleX();
	}

	public float getScaleY () {
		if (scaleOwner == null) return 0;
		return scaleOwner.getScaleY();
	}

	public void setScale (float x, float y) {
		if (scaleOwner == null) return;
		scaleOwner.setScale(x, y);
	}

	//color properties

	protected void enableColor (TintOwner tintOwner) {
		this.tintOwner = tintOwner;
	}

	public boolean isColorSupported () {
		return tintOwner != null;
	}

	public Color getColor () {
		if (tintOwner == null) return Color.CLEAR;
		return tintOwner.getTint();
	}

	public void setColor (Color color) {
		if (tintOwner == null) return;
		tintOwner.setTint(color);
	}

	//rotation properties

	protected void enableRotation (RotationOwner rotationOwner) {
		this.rotationOwner = rotationOwner;
	}

	public boolean isRotationSupported () {
		return rotationOwner != null;
	}

	public float getRotation () {
		if (rotationOwner == null) return 0;
		return rotationOwner.getRotation();
	}

	public void setRotation (float rotation) {
		if (rotationOwner == null) return;
		rotationOwner.setRotation(rotation);
	}

	//flip properties

	protected void enableFlip (FlipOwner flipOwner) {
		this.flipOwner = flipOwner;
	}

	public boolean isFlipSupported () {
		return flipOwner != null;
	}

	public boolean isFlipX () {
		if (flipOwner == null) return false;
		return flipOwner.isFlipX();
	}

	public boolean isFlipY () {
		if (flipOwner == null) return false;
		return flipOwner.isFlipY();
	}

	public void setFlip (boolean x, boolean y) {
		if (flipOwner == null) return;
		flipOwner.setFlip(x, y);
	}

	//others

	public abstract String getEntityName ();

	public Entity getEntity () {
		return entity;
	}

	public boolean compareProxyByUUID (EntityProxy other) {
		return uuid.equals(other.uuid);
	}
}
