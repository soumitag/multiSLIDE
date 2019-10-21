import { Component, OnInit, ViewChild, ElementRef, Renderer2 } from '@angular/core';
import { DatauploadService } from '../dataupload.service'
import { Subscription } from 'rxjs/Subscription'
import { Router } from '@angular/router';
import { CreateSessionService } from '../create-session.service'
import { ServerResponseData } from '../server_response'
import { AnalysisService } from '../analysis.service'
import { FileuploadPayload } from '../fileupload_payload'


@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit {

  private uploadSubscription: Subscription;

  buttonDisabled: boolean = false;
  mslideFileToUpload: File = null;
  analysisUploaded: boolean = false;
  submitTouched: Boolean = false;  // for create analysis
  uploadTouched: Boolean = false;  // for upload analysis

  errorMsgTouched: Boolean = false;
  createrTouched: Boolean = false;
  loaderTouched: Boolean = false;
  openerTouched: Boolean = false;
  demoTouched: Boolean = false;

  analysis_name: string = null;
  response: ServerResponseData = null;
  response_onLoadDemo: ServerResponseData = null;
  response_onLoadAnalysis: ServerResponseData = null;

  create_text: string = '';
  load_text: string = '';
  open_text: string = '';
  demo_text: string = '';

  constructor(private router: Router, 
              private createSessionService: CreateSessionService, 
              private analysisService: AnalysisService, 
              private renderer: Renderer2, 
              private uploadService: DatauploadService) { }

  ngOnInit() { }

  handleFileInput(files: FileList) {
    let fileItem = files.item(0);
    this.mslideFileToUpload = fileItem;
    console.log("file input has changed. The file is", this.mslideFileToUpload);
  }


  show_tab(page_type: string) {
    console.log(page_type)
    if (page_type == 'creater') {
      this.createrTouched = true;
      this.loaderTouched = false;
      this.openerTouched = false;
      this.demoTouched = false;
    } else if (page_type == 'loader') {
      this.loaderTouched = true;
      this.createrTouched = false;
      this.openerTouched = false;
      this.demoTouched = false;
    } else if (page_type == 'opener') {
      this.openerTouched = true;
      this.createrTouched = false;
      this.loaderTouched = false;
      this.demoTouched = false;
    } else if (page_type == 'demo') {
      this.demoTouched = true;
      this.openerTouched = false;
      this.createrTouched = false;
      this.loaderTouched = false;
    }
  }

  handleCreate() {
    //console.log("in handleCreate")
    this.submitTouched = true
    if (this.analysis_name != null) {
      console.log('Homepage says: this.analysis_name = ' + this.analysis_name)
      this.createSession()
      console.log("response is " + this.response)    
    }
  }

  gotoCreateAnalysisIfSucceded() {
    if (this.response) {
      if (this.response.status == 1) {
        this.router.navigateByUrl(
          this.router.createUrlTree(
            ['create_analysis'], { queryParams: { analysis_name: this.analysis_name } }
          )
        );
      }
    }
  }

  createSession() {
    //console.log("inside get session data")
    return this.createSessionService.createSessionData(this.analysis_name)
      .subscribe(resp => this.response = resp, () => console.log("error"), () => this.gotoCreateAnalysisIfSucceded());
  }

  showDemos() {

  }

  loadDemo(demo_number: number){
    // generate random number between 0 - 1
    this.buttonDisabled = true;
    let generated_number = (Math.random()* 50000) + 1;
    console.log(generated_number)

    return this.analysisService.loadDemo(generated_number, demo_number)
      .subscribe(resp => this.response_onLoadDemo = resp, 
                () => console.log("error"), 
                () => this.handleDemo(generated_number));
  }

  handleDemo(generated_number: number) {
    if(this.response_onLoadDemo.status == 1){
      this.router.navigateByUrl(
        this.router.createUrlTree(
          ['visualization_home'], { queryParams: { analysis_name: 'demo_2021158607524066_' + generated_number } }
        )
      );
    } else if (this.response_onLoadDemo.status == 0) {
      alert(this.response_onLoadDemo.message + " " + this.response_onLoadDemo.detailed_reason);
      this.buttonDisabled = false;
    }
    
  }

  showButtonText(button_id) {
    if (button_id == 0) {
      this.create_text = 'Create new visualizations for your data';
    } else if (button_id == 1) {
      this.load_text = 'Load a ".mslide" file to revisit your visualizations';
    } else if (button_id == 2) {
      this.open_text = 'Reopen currently active visualizations';
    } else if (button_id == 3) {
      this.demo_text = 'See a demo of multi-SLIDE with four omics datasets';
    }
  }

  hideButtonText(button_id) {
    if (button_id == 0) {
      this.create_text = ' ';
    } else if (button_id == 1) {
      this.load_text = ' ';
    } else if (button_id == 2) {
      this.open_text = ' ';
    } else if (button_id == 3) {
      this.demo_text = ' ';
    }
  }

  handleUpload() {
    this.uploadTouched = true;
    if (this.mslideFileToUpload != null) {
      console.log("ready to upload analysis");

      var fileupload_payload: FileuploadPayload = {
        'data_action': "",   // sending list name here 
        'analysis_name': this.analysis_name,
        'delimiter': "",
        'upload_type': "",
        'identifier_type': "",
        'filename': this.mslideFileToUpload.name
      }

      this.uploadSubscription = this.uploadService.postMultipartAnalysisData(
        this.mslideFileToUpload, fileupload_payload
      ).subscribe(
        res => this.response_onLoadAnalysis = res,
        () => console.log(this.response_onLoadAnalysis),
        () => this.notifyUploadAnalysisStatus()
      );
    }
  }

  notifyUploadAnalysisStatus() {
    if (this.response_onLoadAnalysis.status == 1) {
      this.analysisUploaded = true;
      //alert(this.response_onLoadAnalysis.detailed_reason);
      this.router.navigateByUrl(
        this.router.createUrlTree(
          ['visualization_home'], { queryParams: { analysis_name: this.response_onLoadAnalysis.message } }
        )
      );
    } else {
      alert(this.response_onLoadAnalysis.message + ": " + this.response_onLoadAnalysis.detailed_reason)      
    }
    
  }


}
