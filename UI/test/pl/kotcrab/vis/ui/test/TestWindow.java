/*******************************************************************************
 * Copyright 2014 Pawel Pastuszak
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
 ******************************************************************************/

package pl.kotcrab.vis.ui.test;

import pl.kotcrab.vis.ui.TableUtils;
import pl.kotcrab.vis.ui.VisTable;
import pl.kotcrab.vis.ui.components.VisCheckBox;
import pl.kotcrab.vis.ui.components.VisLabel;
import pl.kotcrab.vis.ui.components.VisTextButton;
import pl.kotcrab.vis.ui.components.VisTextField;
import pl.kotcrab.vis.ui.components.VisWindow;

import com.badlogic.gdx.scenes.scene2d.Stage;

public class TestWindow extends VisWindow {

	public TestWindow (Stage parent) {
		super(parent, "test window");
		setModal(true);

		TableUtils.setSpaceDefaults(this);
		columnDefaults(0).left();

		// TODO textarea list selectbox progress bar slider scroll pane splitpane tree

		VisLabel label = new VisLabel("label");

		VisTable labelTable = new VisTable(true);
		labelTable.add(label);
		// ---

		VisTextButton normalButton = new VisTextButton("button");
		VisTextButton disabledButton = new VisTextButton("disabled");
		disabledButton.setDisabled(true);

		VisTable buttonTable = new VisTable(true);
		buttonTable.add(normalButton);
		buttonTable.add(disabledButton);

		// ---

		VisCheckBox normalCheckbox = new VisCheckBox(" checkbox");
		VisCheckBox disabledCheckbox = new VisCheckBox(" disabled");
		disabledCheckbox.setDisabled(true);

		VisTable checkboxTable = new VisTable(true);
		checkboxTable.add(normalCheckbox);
		checkboxTable.add(disabledCheckbox);

		// ---

		VisTextField normalTextfield = new VisTextField("textbox");
		VisTextField disabledTextfield = new VisTextField("disabled");
		VisTextField passwordTextfield = new VisTextField("password");
		disabledTextfield.setDisabled(true);
		passwordTextfield.setPasswordMode(true);
		passwordTextfield.setPasswordCharacter('*');

		VisTable textfieldTable = new VisTable(true);
		textfieldTable.add(normalTextfield);
		textfieldTable.add(disabledTextfield);
		textfieldTable.add(passwordTextfield);

		// ---

		add(labelTable).row();
		add(buttonTable).row();
		add(checkboxTable).row();
		add(textfieldTable).row();

		pack();
		setPositionToCenter();
	}
}
