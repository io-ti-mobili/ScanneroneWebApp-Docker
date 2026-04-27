import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, forkJoin, catchError, of } from 'rxjs';
import { SimpleStatDto } from '../dto/simple-stat.dto';
import { ChartDataDto } from '../dto/chart-data.dto';
import { CountryDto } from '../dto/country.dto';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly baseUrl = '/api/metrics';

  constructor(private http: HttpClient) {}

  /**
   * Recupera i dati globali completi dal backend
   */
  getGlobalStats(country?: string, region?: string): Observable<any | null> {
    let params = new HttpParams();
    if (country) params = params.set('country', country);
    if (region) params = params.set('region', region);
    
    return this.http.get<any>(`${this.baseUrl}/stats/global`, { params }).pipe(
      catchError(() => of(null))
    );
  }

  getSecurityChart(country?: string, region?: string): Observable<ChartDataDto[]> {
    let params = new HttpParams();
    if (country) params = params.set('country', country);
    if (region) params = params.set('region', region);
    
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/stats/security`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  getDiscoveriesChart(country?: string, region?: string): Observable<ChartDataDto[]> {
    let params = new HttpParams();
    if (country) params = params.set('country', country);
    if (region) params = params.set('region', region);
    
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/stats/daily-discoveries`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  getTopCountriesChart(limit: number = 5): Observable<ChartDataDto[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/geo/top-countries`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  getTopRegionsChart(country: string, limit: number = 5): Observable<ChartDataDto[]> {
    const params = new HttpParams().set('country', country).set('limit', limit);
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/geo/top-regions`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  getTopCitiesChart(country?: string, region?: string, limit: number = 5): Observable<ChartDataDto[]> {
    let params = new HttpParams().set('limit', limit);
    if (country) params = params.set('country', country);
    if (region) params = params.set('region', region);
    
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/geo/top-cities`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  getTopCitiesWpa3(country?: string, region?: string, limit: number = 5): Observable<ChartDataDto[]> {
    let params = new HttpParams().set('limit', limit);
    if (country) params = params.set('country', country);
    if (region) params = params.set('region', region);
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/geo/top-cities/wpa3`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  getTopInsecureCities(country?: string, region?: string, limit: number = 5): Observable<ChartDataDto[]> {
    let params = new HttpParams().set('limit', limit);
    if (country) params = params.set('country', country);
    if (region) params = params.set('region', region);
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/geo/top-cities/low-security`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  getCountries(limit: number = 100, offset: number = 0): Observable<string[]> {
    const params = new HttpParams().set('limit', limit).set('offset', offset);
    return this.http.get<string[]>(`${this.baseUrl}/geo/countries`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  getRegions(country: string, limit: number = 100, offset: number = 0): Observable<string[]> {
    const params = new HttpParams().set('country', country).set('limit', limit).set('offset', offset);
    return this.http.get<string[]>(`${this.baseUrl}/geo/regions`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  getBandChart(country?: string, region?: string): Observable<ChartDataDto[]> {
    let params = new HttpParams();
    if (country) params = params.set('country', country);
    if (region) params = params.set('region', region);
    
    return this.http.get<any>(`${this.baseUrl}/frequencies`, { params }).pipe(
      catchError(() => of({})),
      map(data => {
        return [
          { label: '2.4 GHz', value: data?.band24Networks || 0 },
          { label: '5 GHz', value: data?.band5Networks || 0 },
          { label: '6 GHz', value: data?.band6Networks || 0 }
        ];
      })
    );
  }
}
