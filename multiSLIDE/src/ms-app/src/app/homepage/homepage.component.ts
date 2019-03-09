import { Component, OnInit, ViewChild, ElementRef, Renderer2 } from '@angular/core';
import { Router } from '@angular/router';
import { CreateSessionService } from '../create-session.service'
import { ServerResponseData } from '../server_response'
import { AnalysisService } from '../analysis.service'

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit {

  slideFileToUpload: File = null;
  submitTouched: Boolean = false;  // for create analysis
  uploadTouched: Boolean = false;  // for upload analysis

  errorMsgTouched: Boolean = false;
  createrTouched: Boolean = false;
  loaderTouched: Boolean = false;
  openerTouched: Boolean = false;

  analysis_name: string = null;
  response: ServerResponseData = null;
  response_onLoadDemo: ServerResponseData = null;

  create_text: string = '';
  load_text: string = '';
  open_text: string = '';
  demo_text: string = '';

  constructor(private router: Router, 
              private createSessionService: CreateSessionService, 
              private analysisService: AnalysisService, 
              private renderer: Renderer2) { }

  ngOnInit() { }

  show_tab(page_type) {
    console.log(page_type)
    if (page_type == 'creater') {
      this.createrTouched = true
      this.loaderTouched = false
      this.openerTouched = false
    } else if (page_type == 'loader') {
      this.loaderTouched = true
      this.createrTouched = false
      this.openerTouched = false
    } else if (page_type == 'opener') {
      this.openerTouched = true
      this.createrTouched = false
      this.loaderTouched = false
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

  handleUpload() {
    this.uploadTouched = true
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

  loadDemo(){
    return this.analysisService.loadDemo()
      .subscribe(resp => this.response_onLoadDemo = resp, 
                () => console.log("error"), 
                () => this.handleDemo());
  }

  handleDemo() {
    if(this.response_onLoadDemo.status == 1){
      this.router.navigateByUrl(
        this.router.createUrlTree(
          ['visualization_home'], { queryParams: { analysis_name: 'demo' } }
        )
      );
    } else if (this.response_onLoadDemo.status == 0) {
      alert("Failed to load demo. Please contact administrator.")
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

}
