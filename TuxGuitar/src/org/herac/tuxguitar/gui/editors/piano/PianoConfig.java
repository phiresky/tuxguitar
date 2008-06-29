package org.herac.tuxguitar.gui.editors.piano;

import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.herac.tuxguitar.gui.TuxGuitar;
import org.herac.tuxguitar.gui.editors.TGPainter;
import org.herac.tuxguitar.gui.system.config.TGConfigKeys;
import org.herac.tuxguitar.gui.system.config.TGConfigManager;
import org.herac.tuxguitar.gui.util.DialogUtils;

public class PianoConfig {
	
	private static final int MINIMUN_CONTROL_WIDTH = 180;
	private static final int MINIMUN_BUTTON_WIDTH = 80;
	private static final int MINIMUN_BUTTON_HEIGHT = 25;
	
	private Color colorNatural;
	private Color colorNotNatural;
	private Color colorNote;
	private Color colorScale;
	
	public PianoConfig(){
		super();
	}
	
	public Color getColorNatural() {
		return this.colorNatural;
	}
	
	public Color getColorNotNatural() {
		return this.colorNotNatural;
	}
	
	public Color getColorNote() {
		return this.colorNote;
	}
	
	public Color getColorScale() {
		return this.colorScale;
	}
	
	public void load(){
		Display display = TuxGuitar.instance().getDisplay();
		TGConfigManager config = TuxGuitar.instance().getConfig();
		this.colorNatural = new Color(display,config.getRGBConfigValue(TGConfigKeys.PIANO_COLOR_KEY_NATURAL));
		this.colorNotNatural = new Color(display,config.getRGBConfigValue(TGConfigKeys.PIANO_COLOR_KEY_NOT_NATURAL));
		this.colorNote = new Color(display,config.getRGBConfigValue(TGConfigKeys.PIANO_COLOR_NOTE));
		this.colorScale = new Color(display,config.getRGBConfigValue(TGConfigKeys.PIANO_COLOR_SCALE));
	}
	
	public void defaults(){
		TGConfigManager config = TuxGuitar.instance().getConfig();
		Properties defaults = config.getDefaults();
		config.setProperty(TGConfigKeys.PIANO_COLOR_KEY_NATURAL,defaults.getProperty(TGConfigKeys.PIANO_COLOR_KEY_NATURAL));
		config.setProperty(TGConfigKeys.PIANO_COLOR_KEY_NOT_NATURAL,defaults.getProperty(TGConfigKeys.PIANO_COLOR_KEY_NOT_NATURAL));
		config.setProperty(TGConfigKeys.PIANO_COLOR_NOTE,defaults.getProperty(TGConfigKeys.PIANO_COLOR_NOTE));
		config.setProperty(TGConfigKeys.PIANO_COLOR_SCALE,defaults.getProperty(TGConfigKeys.PIANO_COLOR_SCALE));
	}
	
	public void save(RGB rgbNatural,RGB rgbNotNatural,RGB rgbNote,RGB rgbScale){
		TGConfigManager config = TuxGuitar.instance().getConfig();
		config.setProperty(TGConfigKeys.PIANO_COLOR_KEY_NATURAL,rgbNatural);
		config.setProperty(TGConfigKeys.PIANO_COLOR_KEY_NOT_NATURAL,rgbNotNatural);
		config.setProperty(TGConfigKeys.PIANO_COLOR_NOTE,rgbNote);
		config.setProperty(TGConfigKeys.PIANO_COLOR_SCALE,rgbScale);
	}
	
	public void dispose(){
		this.colorNatural.dispose();
		this.colorNotNatural.dispose();
		this.colorNote.dispose();
		this.colorScale.dispose();
	}
	
	public void configure(Shell shell) {
		final Shell dialog = DialogUtils.newDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setLayout(new GridLayout());
		dialog.setText(TuxGuitar.getProperty("piano.settings"));
		
		// ----------------------------------------------------------------------
		Group group = new Group(dialog,SWT.SHADOW_ETCHED_IN);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText(TuxGuitar.getProperty("piano.settings"));
		
		// Color
		final RGB rgbNatural = getColorChooser(group,TuxGuitar.getProperty("piano.natural-key-color"), this.colorNatural.getRGB());
		final RGB rgbNotNatural = getColorChooser(group,TuxGuitar.getProperty("piano.not-natural-key-color"), this.colorNotNatural.getRGB());
		final RGB rgbNote = getColorChooser(group,TuxGuitar.getProperty("piano.note-color"), this.colorNote.getRGB());
		final RGB rgbScale = getColorChooser(group,TuxGuitar.getProperty("piano.scale-note-color"), this.colorScale.getRGB());
		
		// ------------------BUTTONS--------------------------
		Composite buttons = new Composite(dialog, SWT.NONE);
		buttons.setLayout(new GridLayout(3, false));
		buttons.setLayoutData(new GridData(SWT.END, SWT.FILL, true, true));
		
		final Button buttonDefaults = new Button(buttons, SWT.PUSH);
		buttonDefaults.setText(TuxGuitar.getProperty("defaults"));
		buttonDefaults.setLayoutData(getButtonData());
		buttonDefaults.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				dialog.dispose();
				defaults();
				applyChanges();
			}
		});
		
		final Button buttonOK = new Button(buttons, SWT.PUSH);
		buttonOK.setText(TuxGuitar.getProperty("ok"));
		buttonOK.setLayoutData(getButtonData());
		buttonOK.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				dialog.dispose();
				
				save(rgbNatural, rgbNotNatural,rgbNote, rgbScale);
				applyChanges();
			}
		});
		
		Button buttonCancel = new Button(buttons, SWT.PUSH);
		buttonCancel.setText(TuxGuitar.getProperty("cancel"));
		buttonCancel.setLayoutData(getButtonData());
		buttonCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				dialog.dispose();
			}
		});
		
		dialog.setDefaultButton( buttonOK );
		
		DialogUtils.openDialog(dialog,DialogUtils.OPEN_STYLE_CENTER | DialogUtils.OPEN_STYLE_PACK | DialogUtils.OPEN_STYLE_WAIT);
	}
	
	protected void applyChanges(){
		this.dispose();
		this.load();
	}
	
	private RGB getColorChooser(final Composite parent,String title,RGB rgb){
		final RGB selection = new RGB(rgb.red,rgb.green,rgb.blue);
		
		Label label = new Label(parent, SWT.NULL);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true));
		label.setText(title);
		
		final Button button = new Button(parent, SWT.PUSH);
		button.setLayoutData(getAlignmentData(MINIMUN_CONTROL_WIDTH,SWT.FILL));
		button.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Color color = new Color(parent.getDisplay(), selection);
				TGPainter painter = new TGPainter(e.gc);
				painter.setBackground(color);
				painter.initPath(TGPainter.PATH_FILL);
				painter.addRectangle(5,5,button.getSize().x - 10,button.getSize().y - 10);
				painter.closePath();
				color.dispose();
			}
		});
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				ColorDialog dlg = new ColorDialog(parent.getShell());
				dlg.setRGB(selection);
				dlg.setText(TuxGuitar.getProperty("choose-color"));
				RGB rgb = dlg.open();
				if (rgb != null) {
					selection.red = rgb.red;
					selection.green = rgb.green;
					selection.blue = rgb.blue;
					button.redraw();
				}
			}
		});
		
		return selection;
	}
	
	private GridData getAlignmentData(int minimumWidth,int horizontalAlignment){
		GridData data = new GridData();
		data.minimumWidth = minimumWidth;
		data.horizontalAlignment = horizontalAlignment;
		data.verticalAlignment = SWT.DEFAULT;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		return data;
	}
	
	protected GridData getButtonData(){
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.minimumWidth = MINIMUN_BUTTON_WIDTH;
		data.minimumHeight = MINIMUN_BUTTON_HEIGHT;
		return data;
	}
}
