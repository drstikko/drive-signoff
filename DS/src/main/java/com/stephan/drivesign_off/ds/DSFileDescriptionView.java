package com.stephan.drivesign_off.ds;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.api.services.drive.model.File;

/**
 * A descirptive table row used for the list of files available from Google Drive.
 *
 * @class   DSFileDescriptionView
 */
public class DSFileDescriptionView extends TableRow {

    /**
     * The title of the file text label.
     *
     * @var TextView
     */
    private TextView _fileTitleLabel;

    /**
     * The data and time label of the file.
     *
     * TextView
     */
    private TextView _fileDateLabel;

    /**
     * The metadata of the file that is represented in the row.
     *
     * @var MetadataBufferResult
     */
    private File _fileMetadata;

    /**
     * Default constructor for a description row.
     *
     * @param context       The context in which to create the row.
     * @param fileMetadata  The file metadata.
     */
    public DSFileDescriptionView(Context context, File fileMetadata) {

        super(context);
        this._fileMetadata = fileMetadata;
        this.setBackgroundColor(Color.LTGRAY);

        // load the layout and add the view
        LayoutInflater inflater = LayoutInflater.from(context);
        this.addView(inflater.inflate(R.layout.view_tablerow, null, false));

        // get the text views and set the associated text
        this._fileTitleLabel = (TextView) this.findViewById(R.id.view_tablerow_title);
        this._fileDateLabel = (TextView) this.findViewById(R.id.view_tablerow_subtitle);
        this._fileTitleLabel.setText(this._fileMetadata.getTitle());
        this._fileDateLabel.setText(DateUtils.formatDateTime(this.getContext(), this._fileMetadata.getCreatedDate().getValue(), 0));
    }

    /**
     * Returns the file metadata associated with the receiver.
     *
     * @return  File
     */
    public File fileMetadata() {
        return this._fileMetadata;
    }
}
