export declare namespace Plotly {
    type Data = any;
    type Layout = any;
    type Config = any;
    interface Figure {
        data: Data[];
        layout: Partial<Layout>;
        frames: Partial<Config>;
    }
    interface PlotlyHTMLElement extends HTMLElement {
        on(event: string, callback: Function): void;
        removeListener(event: string, callback: Function): void;
    }
    interface PlotlyInstance {
        Plots: {
            resize(div: Plotly.PlotlyHTMLElement): void;
        };
        newPlot(div: HTMLDivElement, data: Plotly.Data[], layout?: Partial<Plotly.Layout>, config?: Partial<Plotly.Config>): Promise<PlotlyHTMLElement>;
        plot(div: Plotly.PlotlyHTMLElement, data: Plotly.Data[], layout?: Partial<Plotly.Layout>, config?: Partial<Plotly.Config>): Promise<PlotlyHTMLElement>;
        react(div: Plotly.PlotlyHTMLElement, data: Plotly.Data[], layout?: Partial<Plotly.Layout>, config?: Partial<Plotly.Config>): Promise<PlotlyHTMLElement>;
    }
}
