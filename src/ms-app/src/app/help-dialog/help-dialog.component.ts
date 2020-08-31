import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-help-dialog',
  templateUrl: './help-dialog.component.html',
  styleUrls: ['./help-dialog.component.css']
})
export class HelpDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<HelpDialogComponent>,
  ) { }

  ngOnInit() {
  }

  onOKClick(): void {
    this.dialogRef.close();
  }
}
