<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Desafio extends Model
{
    use HasFactory;

    protected $table = 'desafio';

    protected $fillable = [
        'name',
        'type',
        'image',
        'status',
        'status_type',
    ];

    // 'Time' debe tratarse como objeto datetime
    protected $casts = [
        'time' => 'datetime',
    ];
}
