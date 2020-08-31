import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { HeatmapComponent } from './heatmap/heatmap.component';

import { HeatmapService } from './heatmap.service';
import { DatauploadService } from './dataupload.service'
import { LoadNextComponent } from './load-next/load-next.component';
import { MapContainerComponent } from './map-container/map-container.component';
import { DataUploaderComponent } from './data-uploader/data-uploader.component';
import { CreateAnalysisComponent } from './create-analysis/create-analysis.component';
import { HistogramComponent } from './histogram/histogram.component'
import { HistogramService } from './histogram.service';
import { HomepageComponent } from './homepage/homepage.component';
import { RouterModule, Routes } from '@angular/router';
import { GetPreviewComponent } from './get-preview/get-preview.component';
import { CreateSessionService } from './create-session.service';
import { VisualizationHomeComponent } from './visualization-home/visualization-home.component';
import { SearchPanelComponent } from './search-panel/search-panel.component';
import { SelectionPanelComponent } from './selection-panel/selection-panel.component';
import { SearchService } from './search.service';
import { AssortedService } from './assorted.service';
import { AnalysisService } from './analysis.service';
/*import { ClusteringService } from './clustering.service'; */
import { SignificanceTestingService } from './significance-testing.service';
import { NetworkNeighborhoodComponent } from './network-neighborhood/network-neighborhood.component';
import { LegendsComponent } from './legends/legends.component';

import { MenuPanelComponent } from './menu-panel/menu-panel.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatBadgeModule } from '@angular/material/badge';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTreeModule } from '@angular/material/tree';
import {CdkTableModule} from '@angular/cdk/table';
import {CdkTreeModule} from '@angular/cdk/tree';
import { HierarchicalClusteringComponent } from './hierarchical-clustering/hierarchical-clustering.component';
import { NewListComponent } from './new-list/new-list.component';
import { ListService } from './list.service';
import { ListPanelComponent } from './list-panel/list-panel.component';
import { MapSettingsComponent } from './map-settings/map-settings.component';
import { CreateListComponent } from './create-list/create-list.component';
import { MetadataMapComponent } from './metadata-map/metadata-map.component';
import { UploadListComponent } from './upload-list/upload-list.component';
import { SaveVizComponent } from './save-viz/save-viz.component';
import { SignificanceTestingComponent } from './significance-testing/significance-testing.component';
import { PhenotypeSortingComponent } from './phenotype-sorting/phenotype-sorting.component';
import { SaveWorkspaceComponent } from './save-workspace/save-workspace.component';
import { CloseAnalysisComponent } from './close-analysis/close-analysis.component';
import { DialogBoxComponent } from './dialog-box/dialog-box.component';
import { EnrichmentAnalysisComponent } from './enrichment-analysis/enrichment-analysis.component';
import { AddGenesComponent } from './add-genes/add-genes.component';
import { SearchTabComponent } from './search-tab/search-tab.component';
import { DatasetDialogComponent } from './dataset-dialog/dataset-dialog.component';
import { UploadSearchTabComponent } from './upload-search-tab/upload-search-tab.component';
import { HelpDialogComponent } from './help-dialog/help-dialog.component';
import { DatasetLinkingComponent } from './dataset-linking/dataset-linking.component';
import { MapLinksComponent } from './map-links/map-links.component';
import * as PlotlyJS from 'plotly.js/dist/plotly.js';
import { PlotlyModule } from 'angular-plotly.js';
import { UploadConnectionComponent } from './upload-connection/upload-connection.component';

PlotlyModule.plotlyjs = PlotlyJS;

const appRoutes: Routes = [
  { path: '', component: HomepageComponent },
  { path: 'create_analysis', component: CreateAnalysisComponent },
  { path: 'visualization_home', component: VisualizationHomeComponent },
  { path: 'save_viz', component: SaveVizComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    HeatmapComponent,
    LoadNextComponent,
    MapContainerComponent,
    DataUploaderComponent,
    CreateAnalysisComponent,
    HistogramComponent,
    HomepageComponent,
    GetPreviewComponent,
    VisualizationHomeComponent,
    SearchPanelComponent,
    SelectionPanelComponent,
    NetworkNeighborhoodComponent,
    LegendsComponent,
    MenuPanelComponent,
    HierarchicalClusteringComponent,
    NewListComponent,
    ListPanelComponent,
    MapSettingsComponent,
    CreateListComponent,
    MetadataMapComponent,
    UploadListComponent,
    SaveVizComponent,
    SignificanceTestingComponent,
    PhenotypeSortingComponent,
    SaveWorkspaceComponent,
    CloseAnalysisComponent,
    DialogBoxComponent,
    EnrichmentAnalysisComponent,
    AddGenesComponent,
    SearchTabComponent,
    DatasetDialogComponent,
    UploadSearchTabComponent,
    HelpDialogComponent,
    DatasetLinkingComponent,
    MapLinksComponent,
    UploadConnectionComponent
  ],
  imports: [
    RouterModule.forRoot(
      appRoutes,
      { enableTracing: true, useHash: true } // <-- debugging purposes only
    ), 
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatMenuModule,
    MatListModule,
    MatTooltipModule,
    MatSlideToggleModule,
    MatRadioModule,
    MatTabsModule,
    CommonModule,
    PlotlyModule
  ],
  providers: [
    HeatmapService, 
    DatauploadService,
    HistogramService,
    CreateSessionService,
    SearchService,
    AssortedService,
    AnalysisService,
    ListService,
    /* ClusteringService, */
    SignificanceTestingService
  ],
  bootstrap: [AppComponent],
  exports: [
      CdkTableModule,
      CdkTreeModule,
      MatAutocompleteModule,
      MatBadgeModule,
      MatBottomSheetModule,
      MatButtonModule,
      MatButtonToggleModule,
      MatCardModule,
      MatCheckboxModule,
      MatChipsModule,
      MatStepperModule,
      MatDatepickerModule,
      MatDialogModule,
      MatDividerModule,
      MatExpansionModule,
      MatGridListModule,
      MatIconModule,
      MatInputModule,
      MatListModule,
      MatMenuModule,
      MatNativeDateModule,
      MatPaginatorModule,
      MatProgressBarModule,
      MatProgressSpinnerModule,
      MatRadioModule,
      MatRippleModule,
      MatSelectModule,
      MatSidenavModule,
      MatSliderModule,
      MatSlideToggleModule,
      MatSnackBarModule,
      MatSortModule,
      MatTableModule,
      MatTabsModule,
      MatToolbarModule,
      MatTooltipModule,
      MatTreeModule
    ],
  entryComponents: [
    NewListComponent,
    MapSettingsComponent,
    CreateListComponent,
    MetadataMapComponent,
    HierarchicalClusteringComponent,
    NetworkNeighborhoodComponent,
    UploadListComponent,
    SignificanceTestingComponent,
    PhenotypeSortingComponent,
    SaveWorkspaceComponent,
    CloseAnalysisComponent,
    DialogBoxComponent,
    EnrichmentAnalysisComponent,
    AddGenesComponent,
    DatasetDialogComponent,
    HelpDialogComponent,
    DatasetLinkingComponent
  ]
})
export class AppModule { }
